package dev.paircode.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

record ExecutionRequestedEvent(UUID submissionId, boolean evaluate) {}

@Configuration
@EnableAsync
class ExecutionQueueConfig {
  @Bean("executionPool")
  Executor executionPool() {
    var executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("execution-");
    executor.initialize();
    return executor;
  }
}

@Service
class SubmissionService {
  private final RoomService rooms; private final SubmissionRepository submissions; private final TestCaseRepository cases; private final TestResultRepository results; private final CodeExecutor executor; private final RealtimeService realtime; private final Executor executionPool; private final ApplicationEventPublisher events; private final SessionRepository sessions;
  SubmissionService(RoomService rooms,SubmissionRepository submissions,TestCaseRepository cases,TestResultRepository results,CodeExecutor executor,RealtimeService realtime,@Qualifier("executionPool") Executor executionPool,ApplicationEventPublisher events,SessionRepository sessions){this.rooms=rooms;this.submissions=submissions;this.cases=cases;this.results=results;this.executor=executor;this.realtime=realtime;this.executionPool=executionPool;this.events=events;this.sessions=sessions;}

  @Transactional Submission enqueue(UUID roomId,UUID user,String source,String language,String stdin,boolean evaluate){
    var room=rooms.room(roomId);
    if(room.status==RoomStatus.ENDED)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.GONE,"Session has ended");
    rooms.member(roomId,user);
    var s=new Submission();s.roomId=roomId;s.userId=user;s.problemId=room.problemId;s.sourceCode=source;s.language=language;s.stdin=stdin;s.status=SubmissionStatus.QUEUED;
    submissions.save(s);
    realtime.event(room.roomCode,"run.started",Map.of("submissionId",s.id,"evaluate",evaluate));
    // Publish event so execution starts only AFTER the transaction commits
    events.publishEvent(new ExecutionRequestedEvent(s.id,evaluate));
    return s;
  }

  @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
  void onExecutionRequested(ExecutionRequestedEvent e){
    try {
      executionPool.execute(()->executeNow(e.submissionId(),e.evaluate()));
    } catch(RejectedExecutionException ex){
      // Pool full: mark submission as failed so it doesn't stay QUEUED forever
      submissions.findById(e.submissionId()).ifPresent(s->{
        s.status=SubmissionStatus.RUNTIME_ERROR;s.stderr="Server busy, please try again.";submissions.save(s);
        try{var room=rooms.room(s.roomId);realtime.event(room.roomCode,"run.finished",dto(s));}catch(Exception ignored){}
      });
    }
  }

  /**
   * Submissions cannot survive a process restart: their sandbox is gone. Mark them failed so
   * polling clients see a terminal state instead of "Running in sandbox..." forever.
   */
  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  void failInterruptedSubmissions() {
    var interrupted = submissions.findByStatusIn(
        List.of(SubmissionStatus.QUEUED, SubmissionStatus.RUNNING)
    );
    for (Submission submission : interrupted) {
      submission.status = SubmissionStatus.RUNTIME_ERROR;
      submission.stderr = "Execution was interrupted by a server restart.";
    }
    if (!interrupted.isEmpty()) {
      submissions.saveAll(interrupted);
    }
  }

  Submission get(UUID roomId, UUID submissionId) {
    return submissions.findById(submissionId)
        .filter(submission -> roomId.equals(submission.roomId))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
  }

  // Not @Transactional — manages its own state to avoid detached entity issues
  void executeNow(UUID id,boolean evaluate){
    var s=submissions.findById(id).orElseThrow();
    var room=rooms.room(s.roomId);
    s.status=SubmissionStatus.RUNNING;submissions.save(s);
    try{
      if(evaluate&&s.problemId!=null){
        evaluate(s);
      } else {
        copy(s,executor.execute(new ExecutionRequest(s.language,s.sourceCode,s.stdin)));
      }
      rooms.snapshot(s.roomId,s.userId,s.sourceCode,evaluate?"SUBMIT":"RUN");
    } catch(Exception ex){
      s.status=SubmissionStatus.RUNTIME_ERROR;
      s.stderr=Optional.ofNullable(ex.getMessage()).orElse("Execution error");
      s.compileError=null;
    } finally {
      submissions.save(s);
      // run.finished is ALWAYS sent regardless of outcome
      try{realtime.event(room.roomCode,"run.finished",dto(s));}catch(Exception ignored){}
    }
  }

  private void evaluate(Submission submission) {
    var tests = cases.findByProblemIdOrderByOrdinal(submission.problemId);
    if (tests.isEmpty()) {
      submission.status = SubmissionStatus.WRONG_ANSWER;
      submission.score = 0;
      submission.stdout = "0/0 tests passed";
      submission.stderr = null;
      submission.compileError = null;
      submission.exitCode = null;
      submission.executionTimeMs = 0L;
      submission.memoryBytes = 0L;
      return;
    }

    try (PreparedExecution execution = executor.prepare(submission.language, submission.sourceCode)) {
      CompileResult compile = execution.compile();
      if (!compile.success()) {
        submission.status = SubmissionStatus.COMPILE_ERROR;
        submission.compileError = compile.message();
        submission.stdout = "";
        submission.stderr = null;
        submission.exitCode = compile.exitCode();
        submission.executionTimeMs = compile.executionTimeMs();
        submission.memoryBytes = 0L;
        submission.score = 0;
        return;
      }

      int passed = 0;
      long maxRuntimeMs = 0;
      Integer firstFailureExitCode = null;
      String firstFailureStderr = null;
      var failureStatuses = new ArrayList<SubmissionStatus>();

      for (TestCase test : tests) {
        ExecutionResult output = execution.run(test.input);
        boolean matches = output.status() == SubmissionStatus.SUCCESS
            && normalize(output.stdout()).equals(normalize(test.expectedOutput));

        // The actual output is stored for every test but the DTO only exposes visible tests.
        results.save(new TestResult(
            submission.id,
            test.id,
            matches,
            output.executionTimeMs(),
            output.stdout()
        ));

        maxRuntimeMs = Math.max(maxRuntimeMs, output.executionTimeMs());
        if (matches) {
          passed++;
        } else {
          failureStatuses.add(Evaluation.testStatus(output, false));
          if (firstFailureStderr == null) {
            firstFailureStderr = Optional.ofNullable(output.stderr()).orElse("");
            firstFailureExitCode = output.exitCode();
          }
        }
      }

      submission.score = (int) Math.round(100.0 * passed / tests.size());
      submission.status = Evaluation.overallStatus(failureStatuses);
      submission.stdout = passed + "/" + tests.size() + " tests passed";
      submission.stderr = firstFailureStderr;
      submission.compileError = null;
      submission.exitCode = firstFailureExitCode;
      submission.executionTimeMs = maxRuntimeMs;
      submission.memoryBytes = 0L;

      sessions.findByRoomId(submission.roomId).ifPresent(session -> {
        if (session.score == null || submission.score > session.score) {
          session.score = submission.score;
          sessions.save(session);
        }
      });
    }
  }

  Map<String,Object> dto(Submission s){
    var dto=new LinkedHashMap<String,Object>();
    dto.put("id",s.id);
    dto.put("status",s.status);
    dto.put("stdout",s.stdout);
    dto.put("stderr",s.stderr);
    dto.put("compileError",s.compileError);
    dto.put("exitCode",s.exitCode);
    dto.put("executionTimeMs",s.executionTimeMs);
    dto.put("memoryBytes",s.memoryBytes);
    dto.put("score",s.score);

    var rows=new ArrayList<TestRow>();
    for(TestResult result:results.findBySubmissionId(s.id)){
      cases.findById(result.testCaseId).ifPresent(testCase->rows.add(new TestRow(testCase,result)));
    }
    rows.sort(Comparator.comparingInt(row->row.testCase().ordinal));

    var testResults=new ArrayList<Map<String,Object>>();
    for(TestRow row:rows){
      var test=new LinkedHashMap<String,Object>();
      test.put("testCaseId",row.result().testCaseId);
      test.put("passed",row.result().passed);
      test.put("runtimeMs",row.result().runtimeMs);
      if(!row.testCase().hidden){
        test.put("input",row.testCase().input);
        test.put("expectedOutput",row.testCase().expectedOutput);
        test.put("actualOutput",row.result().actualOutput);
      }
      testResults.add(test);
    }
    dto.put("testResults",testResults);
    return dto;
  }

  private void copy(Submission s,ExecutionResult r){s.status=r.status();s.stdout=r.stdout();s.stderr=r.stderr();s.compileError=r.compileError();s.exitCode=r.exitCode();s.executionTimeMs=r.executionTimeMs();s.memoryBytes=r.memoryBytes();} private String normalize(String x){return Optional.ofNullable(x).orElse("").replace("\r\n","\n").trim();}

  private record TestRow(TestCase testCase, TestResult result) {}
}
