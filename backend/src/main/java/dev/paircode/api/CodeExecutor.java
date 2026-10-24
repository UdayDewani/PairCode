package dev.paircode.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

record ExecutionRequest(String language,String sourceCode,String stdin) {}
record ExecutionResult(SubmissionStatus status,String stdout,String stderr,String compileError,int exitCode,long executionTimeMs,long memoryBytes) {}
interface CodeExecutor { ExecutionResult execute(ExecutionRequest request); }
@Component
class DockerCodeExecutor implements CodeExecutor {
  private final boolean enabled; private final String docker;
  DockerCodeExecutor(@Value("${app.runner-enabled}") boolean enabled,@Value("${app.docker-command}") String docker){this.enabled=enabled;this.docker=docker;}
  @Override public ExecutionResult execute(ExecutionRequest r){
    if(!enabled)return new ExecutionResult(SubmissionStatus.RUNTIME_ERROR,"","Runner is disabled. Set RUNNER_ENABLED=true.",null,-1,0,0);
    Path dir=null;long started=System.nanoTime();try{dir=Files.createTempDirectory("paircode-run-");String file=switch(r.language()){case "JAVA"->"Main.java";case "CPP"->"main.cpp";default->"main.py";};Files.writeString(dir.resolve(file),r.sourceCode(),StandardCharsets.UTF_8);Files.writeString(dir.resolve("input.txt"),Optional.ofNullable(r.stdin()).orElse(""),StandardCharsets.UTF_8);
      String image=switch(r.language()){case "JAVA"->"paircode-runner-java";case "CPP"->"paircode-runner-cpp";default->"paircode-runner-python";};String command=switch(r.language()){case "JAVA"->"javac Main.java && timeout 5 java Main < input.txt";case "CPP"->"g++ -O2 -std=c++20 main.cpp -o main && timeout 5 ./main < input.txt";default->"timeout 5 python3 main.py < input.txt";};
      var p=new ProcessBuilder(docker,"run","--rm","--network","none","--read-only","--pids-limit","64","--memory","256m","--cpus","1","--tmpfs","/tmp:rw,noexec,nosuid,size=64m","-v",dir.toAbsolutePath()+":/workspace:rw",image,"sh","-lc",command).redirectErrorStream(false).start();boolean finished=p.waitFor(7,TimeUnit.SECONDS);if(!finished){p.destroyForcibly();return new ExecutionResult(SubmissionStatus.TIME_LIMIT_EXCEEDED,"","Process timed out",null,-1,elapsed(started),0);}String out=limited(p.getInputStream()),err=limited(p.getErrorStream());int exit=p.exitValue();SubmissionStatus status=exit==0?SubmissionStatus.SUCCESS:(err.contains("error:")?SubmissionStatus.COMPILE_ERROR:SubmissionStatus.RUNTIME_ERROR);return new ExecutionResult(status,out,err,status==SubmissionStatus.COMPILE_ERROR?err:null,exit,elapsed(started),0);
    }catch(Exception e){return new ExecutionResult(SubmissionStatus.RUNTIME_ERROR,"",e.getMessage(),null,-1,elapsed(started),0);}finally{if(dir!=null)try(var paths=Files.walk(dir)){paths.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(IOException ignored){}});}catch(IOException ignored){}}
  }
  private long elapsed(long start){return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);} private String limited(InputStream stream)throws IOException{byte[] b=stream.readNBytes(64*1024+1);return new String(b,0,Math.min(b.length,64*1024),StandardCharsets.UTF_8);}
}
