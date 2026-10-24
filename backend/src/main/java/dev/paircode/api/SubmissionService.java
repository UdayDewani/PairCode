package dev.paircode.api;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.*;
import java.util.concurrent.*;

record ExecutionRequestedEvent(UUID submissionId, boolean evaluate) {}

@Configuration @EnableAsync
class ExecutionQueueConfig { @Bean("executionPool") Executor executionPool(){var e=new ThreadPoolTaskExecutor();e.setCorePoolSize(2);e.setMaxPoolSize(4);e.setQueueCapacity(100);e.setThreadNamePrefix("execution-");e.initialize();return e;} }

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
        rooms.room(s.roomId);// just to get roomCode
        try{var room=rooms.room(s.roomId);realtime.event(room.roomCode,"run.finished",dto(s));}catch(Exception ignored){}
      });
    }
  }

  // Not @Transactional — manages its own state to avoid detached entity issues
  void executeNow(UUID id,boolean evaluate){
    var s=submissions.findById(id).orElseThrow();
    var room=rooms.room(s.roomId);
    s.status=SubmissionStatus.RUNNING;submissions.save(s);
    try{
      if(evaluate&&s.problemId!=null){
        var tests=cases.findByProblemIdOrderByOrdinal(s.problemId);
        int passed=0; SubmissionStatus overallStatus=SubmissionStatus.SUCCESS;
        for(var test:tests){
          var out=executor.execute(new ExecutionRequest(s.language,s.sourceCode,test.input));
          boolean pass=out.status()==SubmissionStatus.SUCCESS&&normalize(out.stdout()).equals(normalize(test.expectedOutput));
          // Only store visible test output; never expose hidden test output
          results.save(new TestResult(s.id,test.id,pass,out.executionTimeMs(),test.hidden?null:out.stdout()));
          if(pass)passed++; else if(overallStatus==SubmissionStatus.SUCCESS)overallStatus=out.status();
        }
        s.score=tests.isEmpty()?0:(int)Math.round(100.0*passed/tests.size());
        s.status=overallStatus;
        // Store best score on SessionRecord
        sessions.findByRoomId(s.roomId).ifPresent(sr->{if(sr.score==null||s.score>sr.score){sr.score=s.score;sessions.save(sr);}});
      } else {
        copy(s,executor.execute(new ExecutionRequest(s.language,s.sourceCode,s.stdin)));
      }
      rooms.snapshot(s.roomId,s.userId,s.sourceCode,evaluate?"SUBMIT":"RUN");
    } catch(Exception ex){
      s.status=SubmissionStatus.RUNTIME_ERROR; s.stderr=Optional.ofNullable(ex.getMessage()).orElse("Execution error");
    } finally {
      submissions.save(s);
      // run.finished is ALWAYS sent regardless of outcome
      try{realtime.event(room.roomCode,"run.finished",dto(s));}catch(Exception ignored){}
    }
  }

  Map<String,Object> dto(Submission s){var m=new LinkedHashMap<String,Object>();m.put("id",s.id);m.put("status",s.status);m.put("stdout",s.stdout);m.put("stderr",s.stderr);m.put("compileError",s.compileError);m.put("exitCode",s.exitCode);m.put("executionTimeMs",s.executionTimeMs);m.put("memoryBytes",s.memoryBytes);m.put("score",s.score);m.put("testResults",results.findBySubmissionId(s.id).stream().map(r->{var tc=cases.findById(r.testCaseId).orElseThrow();var x=new LinkedHashMap<String,Object>();x.put("testCaseId",r.testCaseId);x.put("passed",r.passed);x.put("runtimeMs",r.runtimeMs);if(!tc.hidden){x.put("actualOutput",r.actualOutput);x.put("expectedOutput",tc.expectedOutput);}return x;}).toList());return m;}
  private void copy(Submission s,ExecutionResult r){s.status=r.status();s.stdout=r.stdout();s.stderr=r.stderr();s.compileError=r.compileError();s.exitCode=r.exitCode();s.executionTimeMs=r.executionTimeMs();s.memoryBytes=r.memoryBytes();} private String normalize(String x){return Optional.ofNullable(x).orElse("").replace("\r\n","\n").trim();}
}
