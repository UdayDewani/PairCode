package dev.paircode.api;

import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.*;

@Configuration @EnableAsync
class ExecutionQueueConfig { @Bean("executionPool") Executor executionPool(){var e=new ThreadPoolTaskExecutor();e.setCorePoolSize(2);e.setMaxPoolSize(4);e.setQueueCapacity(100);e.setThreadNamePrefix("execution-");e.initialize();return e;} }
@Service
class SubmissionService {
  private final RoomService rooms; private final SubmissionRepository submissions; private final TestCaseRepository cases; private final TestResultRepository results; private final CodeExecutor executor; private final RealtimeService realtime; private final Executor executionPool;
  SubmissionService(RoomService rooms,SubmissionRepository submissions,TestCaseRepository cases,TestResultRepository results,CodeExecutor executor,RealtimeService realtime,@Qualifier("executionPool") Executor executionPool){this.rooms=rooms;this.submissions=submissions;this.cases=cases;this.results=results;this.executor=executor;this.realtime=realtime;this.executionPool=executionPool;}
  @Transactional Submission enqueue(UUID roomId,UUID user,String source,String language,String stdin,boolean evaluate){var room=rooms.room(roomId);rooms.member(roomId,user);var s=new Submission();s.roomId=roomId;s.userId=user;s.problemId=room.problemId;s.sourceCode=source;s.language=language;s.stdin=stdin;s.status=SubmissionStatus.QUEUED;submissions.save(s);realtime.event(room.roomCode,"run.started",Map.of("submissionId",s.id,"evaluate",evaluate));executionPool.execute(()->executeNow(s.id,evaluate));return s;}
  @Transactional void executeNow(UUID id,boolean evaluate){var s=submissions.findById(id).orElseThrow();s.status=SubmissionStatus.RUNNING;submissions.save(s);var room=rooms.room(s.roomId);if(evaluate&&s.problemId!=null){var tests=cases.findByProblemIdOrderByOrdinal(s.problemId);int passed=0;ExecutionResult last=null;for(var test:tests){var out=executor.execute(new ExecutionRequest(s.language,s.sourceCode,test.input));last=out;boolean pass=out.status()==SubmissionStatus.SUCCESS&&normalize(out.stdout()).equals(normalize(test.expectedOutput));results.save(new TestResult(s.id,test.id,pass,out.executionTimeMs(),test.hidden?null:out.stdout()));if(pass)passed++;}s.score=tests.isEmpty()?0:(int)Math.round(100.0*passed/tests.size());if(last!=null)copy(s,last);else s.status=SubmissionStatus.SUCCESS;}else copy(s,executor.execute(new ExecutionRequest(s.language,s.sourceCode,s.stdin)));submissions.save(s);rooms.snapshot(s.roomId,s.userId,s.sourceCode,evaluate?"SUBMIT":"RUN");realtime.event(room.roomCode,"run.finished",dto(s));}
  Map<String,Object> dto(Submission s){var m=new LinkedHashMap<String,Object>();m.put("id",s.id);m.put("status",s.status);m.put("stdout",s.stdout);m.put("stderr",s.stderr);m.put("compileError",s.compileError);m.put("exitCode",s.exitCode);m.put("executionTimeMs",s.executionTimeMs);m.put("memoryBytes",s.memoryBytes);m.put("score",s.score);m.put("testResults",results.findBySubmissionId(s.id).stream().map(r->{var tc=cases.findById(r.testCaseId).orElseThrow();var x=new LinkedHashMap<String,Object>();x.put("testCaseId",r.testCaseId);x.put("passed",r.passed);x.put("runtimeMs",r.runtimeMs);if(!tc.hidden){x.put("actualOutput",r.actualOutput);x.put("expectedOutput",tc.expectedOutput);}return x;}).toList());return m;}
  private void copy(Submission s,ExecutionResult r){s.status=r.status();s.stdout=r.stdout();s.stderr=r.stderr();s.compileError=r.compileError();s.exitCode=r.exitCode();s.executionTimeMs=r.executionTimeMs();s.memoryBytes=r.memoryBytes();} private String normalize(String x){return Optional.ofNullable(x).orElse("").replace("\r\n","\n").trim();}
}
