package dev.paircode.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger log = LoggerFactory.getLogger(DockerCodeExecutor.class);
  private static final int OUTPUT_LIMIT = 64 * 1024; // 64 KB
  private final boolean enabled; private final String docker;
  DockerCodeExecutor(@Value("${app.runner-enabled}") boolean enabled,@Value("${app.docker-command}") String docker){this.enabled=enabled;this.docker=docker;}

  @Override public ExecutionResult execute(ExecutionRequest r){
    if(!enabled) return new ExecutionResult(SubmissionStatus.RUNTIME_ERROR,"","Runner is disabled. Set RUNNER_ENABLED=true.",null,-1,0,0);
    Path dir=null; long started=System.nanoTime();
    try{
      dir=Files.createTempDirectory("paircode-run-");
      String file=switch(r.language()){case "JAVA"->"Main.java";case "CPP"->"main.cpp";default->"main.py";};
      Files.writeString(dir.resolve(file),r.sourceCode(),StandardCharsets.UTF_8);
      Files.writeString(dir.resolve("input.txt"),Optional.ofNullable(r.stdin()).orElse(""),StandardCharsets.UTF_8);
      String image=switch(r.language()){case "JAVA"->"paircode-runner-java";case "CPP"->"paircode-runner-cpp";default->"paircode-runner-python";};
      String containerName="paircode-run-"+UUID.randomUUID().toString().substring(0,8);

      // Step 1: Compile (Java and C++ only)
      if("JAVA".equals(r.language())||"CPP".equals(r.language())){
        String compileCmd=switch(r.language()){case "JAVA"->"javac Main.java";default->"g++ -O2 -std=c++20 main.cpp -o main";};
        var compileResult=runInDocker(containerName+"-c",image,dir,"sh","-lc",compileCmd,30,started);
        if(compileResult.exitValue()!=0){
          String err=drainLimited(compileResult.getInputStream(),compileResult.getErrorStream());
          return new ExecutionResult(SubmissionStatus.COMPILE_ERROR,"",err,err,compileResult.exitValue(),elapsed(started),0);
        }
        drainLimited(compileResult.getInputStream(),compileResult.getErrorStream()); // discard compile stdout
      }

      // Step 2: Run
      String runCmd=switch(r.language()){
        case "JAVA"->"timeout 5 java Main < input.txt";
        case "CPP"->"timeout 5 ./main < input.txt";
        default->"timeout 5 python3 main.py < input.txt";
      };
      String runContainerName=containerName+"-r";
      var p=runInDocker(runContainerName,image,dir,"sh","-lc",runCmd,8,started);
      boolean finished=p.waitFor(8,TimeUnit.SECONDS);
      if(!finished){
        killContainer(runContainerName);p.destroyForcibly();
        return new ExecutionResult(SubmissionStatus.TIME_LIMIT_EXCEEDED,"","Process timed out",null,124,elapsed(started),0);
      }
      String stdout=drainStream(p.getInputStream());
      String stderr=drainStream(p.getErrorStream());
      int exit=p.exitValue();
      SubmissionStatus status=switch(exit){
        case 0->SubmissionStatus.SUCCESS;
        case 124->SubmissionStatus.TIME_LIMIT_EXCEEDED;
        case 137->SubmissionStatus.TIME_LIMIT_EXCEEDED; // OOM killed
        default->SubmissionStatus.RUNTIME_ERROR;
      };
      return new ExecutionResult(status,truncate(stdout),truncate(stderr),null,exit,elapsed(started),0);
    }catch(Exception e){
      return new ExecutionResult(SubmissionStatus.RUNTIME_ERROR,"",e.getMessage(),null,-1,elapsed(started),0);
    }finally{
      if(dir!=null)try(var paths=Files.walk(dir)){paths.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(IOException ignored){}});}catch(IOException ignored){}
    }
  }

  private Process runInDocker(String name,String image,Path dir,String... cmd) throws IOException {
    var args=new ArrayList<String>();
    args.add(docker);args.add("run");args.add("--rm");args.add("--name");args.add(name);
    args.add("--network");args.add("none");
    args.add("--read-only");
    args.add("--pids-limit");args.add("64");
    args.add("--memory");args.add("256m");
    args.add("--cpus");args.add("1");
    args.add("--cap-drop");args.add("ALL");
    args.add("--security-opt");args.add("no-new-privileges");
    args.add("--user");args.add("65534:65534"); // nobody
    args.add("--tmpfs");args.add("/tmp:rw,noexec,nosuid,size=64m");
    args.add("-v");args.add(dir.toAbsolutePath()+":/workspace:rw");
    args.add("-w");args.add("/workspace");
    args.add(image);
    args.addAll(Arrays.asList(cmd));
    return new ProcessBuilder(args).redirectErrorStream(false).start();
  }

  /** Overload that also waits (for compile step) */
  private Process runInDocker(String name,String image,Path dir,String entrypoint,String flag,String cmd,int timeoutSec,long started) throws IOException,InterruptedException {
    var p=runInDocker(name,image,dir,entrypoint,flag,cmd);
    boolean done=p.waitFor(timeoutSec,TimeUnit.SECONDS);
    if(!done){killContainer(name);p.destroyForcibly();}
    return p;
  }

  private void killContainer(String name){try{new ProcessBuilder(docker,"kill",name).start().waitFor(3,TimeUnit.SECONDS);}catch(Exception ignored){}}

  private String drainLimited(InputStream stdout,InputStream stderr){
    try{
      var se=new StringBuilder();
      new Thread(()->{try{se.append(drainStream(stdout));}catch(Exception ignored){}}).start();
      var err=drainStream(stderr);
      return truncate(se.toString()+err);
    }catch(Exception e){return "";}
  }
  private String drainStream(InputStream in) throws IOException {
    byte[] b=in.readNBytes(OUTPUT_LIMIT+1); return new String(b,0,Math.min(b.length,OUTPUT_LIMIT),StandardCharsets.UTF_8);
  }
  private String truncate(String s){return s==null?null:(s.length()>OUTPUT_LIMIT?s.substring(0,OUTPUT_LIMIT)+"…[truncated]":s);}
  private long elapsed(long start){return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);}
}
