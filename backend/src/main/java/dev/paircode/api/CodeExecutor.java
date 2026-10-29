package dev.paircode.api;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

record ExecutionRequest(String language, String sourceCode, String stdin) {}

record ExecutionResult(
    SubmissionStatus status,
    String stdout,
    String stderr,
    String compileError,
    int exitCode,
    long executionTimeMs,
    long memoryBytes
) {}

interface CodeExecutor {
  ExecutionResult execute(ExecutionRequest request);
}

final class RunnerImages {
  static final String PYTHON = "paircode-runner-python";
  static final String JAVA = "paircode-runner-java";
  static final String CPP = "paircode-runner-cpp";

  private RunnerImages() {}

  static String forLanguage(String language) {
    return switch (language) {
      case "JAVA" -> JAVA;
      case "CPP" -> CPP;
      default -> PYTHON;
    };
  }

  static List<String> all() {
    return List.of(PYTHON, JAVA, CPP);
  }

  static String buildCommand(String image) {
    String directory = switch (image) {
      case JAVA -> "java";
      case CPP -> "cpp";
      default -> "python";
    };
    return "docker build -t " + image + " backend/runner-images/" + directory;
  }
}

@Component
class DockerCodeExecutor implements CodeExecutor {
  static final int OUTPUT_LIMIT = 64 * 1024;
  static final String TRUNCATED_MARKER = "\n...[output truncated]";

  private static final Logger log = LoggerFactory.getLogger(DockerCodeExecutor.class);
  private static final Duration COMPILE_TIMEOUT = Duration.ofSeconds(30);
  private static final Duration RUN_TIMEOUT = Duration.ofSeconds(8);
  private static final Duration STALE_AFTER = Duration.ofSeconds(60);
  private static final String SANDBOX_LABEL = "paircode.sandbox=true";

  private final boolean enabled;
  private final String docker;
  private final Path workDir;

  DockerCodeExecutor(
      @Value("${app.runner-enabled}") boolean enabled,
      @Value("${app.docker-command}") String docker,
      @Value("${app.runner-work-dir}") String workDir
  ) {
    this.enabled = enabled;
    this.docker = docker;
    this.workDir = Path.of(workDir).toAbsolutePath().normalize();
  }

  @PostConstruct
  void createWorkDirectory() {
    if (!enabled) {
      return;
    }
    try {
      Files.createDirectories(workDir);
      log.info("Runner work directory is {}", workDir);
    } catch (IOException e) {
      log.error("Runner is enabled but its work directory {} cannot be created: {}", workDir, e.getMessage());
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  void removeLeftoversOnStartup() {
    cleanupStaleRuns();
  }

  @Scheduled(initialDelay = 300_000, fixedDelay = 300_000)
  void removeLeftoversPeriodically() {
    cleanupStaleRuns();
  }

  @Override
  public ExecutionResult execute(ExecutionRequest request) {
    if (!enabled) {
      return new ExecutionResult(
          SubmissionStatus.RUNTIME_ERROR,
          "",
          "Runner is disabled. Set RUNNER_ENABLED=true.",
          null,
          -1,
          0,
          0
      );
    }

    Path runDirectory = null;
    long started = System.nanoTime();
    String containerPrefix = "paircode-run-" + UUID.randomUUID().toString().substring(0, 8);

    try {
      ensureWorkDirectory();
      runDirectory = Files.createTempDirectory(workDir, "run-");
      relaxPermissions(runDirectory);

      String sourceFile = switch (request.language()) {
        case "JAVA" -> "Main.java";
        case "CPP" -> "main.cpp";
        default -> "main.py";
      };
      Path sourcePath = runDirectory.resolve(sourceFile);
      Files.writeString(sourcePath, request.sourceCode(), StandardCharsets.UTF_8);
      makeReadable(sourcePath);

      Path inputPath = runDirectory.resolve("input.txt");
      Files.writeString(inputPath, Optional.ofNullable(request.stdin()).orElse(""), StandardCharsets.UTF_8);
      makeReadable(inputPath);

      String image = RunnerImages.forLanguage(request.language());

      if ("JAVA".equals(request.language()) || "CPP".equals(request.language())) {
        String compileCommand = "JAVA".equals(request.language())
            ? "javac Main.java"
            : "g++ -O2 -std=c++20 main.cpp -o main";
        ContainerRun compile = runContainer(containerPrefix + "-c", image, runDirectory, compileCommand, COMPILE_TIMEOUT);

        if (compile.timedOut()) {
          String message = "Compilation timed out after " + COMPILE_TIMEOUT.toSeconds() + " seconds.";
          return new ExecutionResult(
              SubmissionStatus.COMPILE_ERROR,
              compile.stdout(),
              message,
              joinOutput(compile.stderr(), compile.stdout()),
              124,
              elapsed(started),
              0
          );
        }
        if (compile.exitCode() != 0) {
          String stderr = compile.oomKilled()
              ? prepend("Compiler ran out of memory (256 MB limit).\n", compile.stderr())
              : compile.stderr();
          String combined = joinOutput(stderr, compile.stdout());
          return new ExecutionResult(
              SubmissionStatus.COMPILE_ERROR,
              "",
              combined,
              combined,
              compile.exitCode(),
              elapsed(started),
              0
          );
        }
      }

      String runCommand = switch (request.language()) {
        case "JAVA" -> "timeout 5 java -Xmx128m -XX:+UseSerialGC Main < input.txt";
        case "CPP" -> "timeout 5 ./main < input.txt";
        default -> "timeout 5 python3 main.py < input.txt";
      };
      ContainerRun run = runContainer(containerPrefix + "-r", image, runDirectory, runCommand, RUN_TIMEOUT);

      if (run.timedOut() || run.exitCode() == 124) {
        return new ExecutionResult(
            SubmissionStatus.TIME_LIMIT_EXCEEDED,
            run.stdout(),
            "Time limit exceeded (5 seconds).",
            null,
            124,
            elapsed(started),
            0
        );
      }
      if (run.oomKilled()) {
        return new ExecutionResult(
            SubmissionStatus.RUNTIME_ERROR,
            run.stdout(),
            prepend("Memory limit exceeded: the process was killed after using more than 256 MB.\n", run.stderr()),
            null,
            137,
            elapsed(started),
            0
        );
      }
      if (run.exitCode() != 0) {
        return new ExecutionResult(
            SubmissionStatus.RUNTIME_ERROR,
            run.stdout(),
            run.stderr(),
            null,
            run.exitCode(),
            elapsed(started),
            0
        );
      }
      return new ExecutionResult(
          SubmissionStatus.SUCCESS,
          run.stdout(),
          run.stderr(),
          null,
          0,
          elapsed(started),
          0
      );
    } catch (IOException e) {
      return new ExecutionResult(
          SubmissionStatus.RUNTIME_ERROR,
          "",
          "Could not start the Docker runner: " + e.getMessage(),
          null,
          -1,
          elapsed(started),
          0
      );
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return new ExecutionResult(
          SubmissionStatus.RUNTIME_ERROR,
          "",
          "Execution was interrupted.",
          null,
          -1,
          elapsed(started),
          0
      );
    } catch (RuntimeException e) {
      log.error("Unexpected runner failure", e);
      return new ExecutionResult(
          SubmissionStatus.RUNTIME_ERROR,
          "",
          "Runner error: " + e.getMessage(),
          null,
          -1,
          elapsed(started),
          0
      );
    } finally {
      deleteRecursively(runDirectory);
    }
  }

  void cleanupStaleRuns() {
    if (!enabled) {
      return;
    }
    removeStaleContainers();
    removeStaleDirectories();
  }

  private void ensureWorkDirectory() throws IOException {
    if (!Files.isDirectory(workDir)) {
      Files.createDirectories(workDir);
    }
  }

  private ContainerRun runContainer(
      String name,
      String image,
      Path directory,
      String shellCommand,
      Duration timeout
  ) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(docker);
    command.add("run");
    command.add("--name");
    command.add(name);
    command.add("--label");
    command.add(SANDBOX_LABEL);
    command.add("--label");
    command.add("paircode.created=" + Instant.now().getEpochSecond());
    command.add("--pull=never");
    command.add("--network");
    command.add("none");
    command.add("--read-only");
    command.add("--pids-limit");
    command.add("64");
    command.add("--memory");
    command.add("256m");
    command.add("--memory-swap");
    command.add("256m");
    command.add("--cpus");
    command.add("1");
    command.add("--ulimit");
    command.add("nofile=256:256");
    command.add("--ulimit");
    command.add("nproc=64:64");
    command.add("--cap-drop");
    command.add("ALL");
    command.add("--security-opt");
    command.add("no-new-privileges");
    command.add("--user");
    command.add("65534:65534");
    command.add("--tmpfs");
    command.add("/tmp:rw,noexec,nosuid,size=64m");
    command.add("-v");
    command.add(directory.toAbsolutePath() + ":/workspace:rw");
    command.add("-w");
    command.add("/workspace");
    command.add(image);
    command.add("sh");
    command.add("-c");
    command.add(shellCommand);

    Process process = new ProcessBuilder(command).redirectErrorStream(false).start();
    StreamCollector stdout = new StreamCollector(process.getInputStream());
    StreamCollector stderr = new StreamCollector(process.getErrorStream());
    boolean timedOut = false;
    int exitCode;

    try (ExecutorService streams = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<?> stdoutTask = streams.submit(stdout);
      Future<?> stderrTask = streams.submit(stderr);

      try {
        if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
          timedOut = true;
          removeContainer(name);
          process.destroyForcibly();
          closeQuietly(process.getInputStream());
          closeQuietly(process.getErrorStream());
          process.waitFor(2, TimeUnit.SECONDS);
        }
      } catch (InterruptedException e) {
        removeContainer(name);
        process.destroyForcibly();
        closeQuietly(process.getInputStream());
        closeQuietly(process.getErrorStream());
        throw e;
      }

      awaitCollector(stdoutTask);
      awaitCollector(stderrTask);
      exitCode = timedOut || process.isAlive() ? 124 : process.exitValue();

      boolean oomKilled = !timedOut && exitCode != 0 && inspectOomKilled(name);
      return new ContainerRun(exitCode, stdout.text(), stderr.text(), timedOut, oomKilled);
    } finally {
      removeContainer(name);
    }
  }

  private boolean inspectOomKilled(String name) {
    CommandOutput result = runCommand(
        List.of(docker, "inspect", "--format", "{{.State.OOMKilled}}", name),
        Duration.ofSeconds(5)
    );
    return result.exitCode() == 0 && "true".equalsIgnoreCase(result.output().trim());
  }

  private void removeContainer(String name) {
    runCommand(List.of(docker, "rm", "-f", name), Duration.ofSeconds(5));
  }

  private void removeStaleContainers() {
    CommandOutput listing = runCommand(
        List.of(docker, "ps", "-a", "--filter", "name=paircode-run-", "--format", "{{.ID}}"),
        Duration.ofSeconds(15)
    );
    if (listing.exitCode() != 0) {
      log.warn("Could not list leftover runner containers: {}", concise(listing.output()));
      return;
    }
    Instant cutoff = Instant.now().minus(STALE_AFTER);
    for (String id : listing.output().lines().map(String::trim).filter(line -> !line.isEmpty()).toList()) {
      Instant created = containerCreatedAt(id);
      if (created != null && created.isBefore(cutoff)) {
        runCommand(List.of(docker, "rm", "-f", id), Duration.ofSeconds(10));
        log.info("Removed stale runner container {}", id);
      }
    }
  }

  private Instant containerCreatedAt(String id) {
    CommandOutput result = runCommand(
        List.of(docker, "inspect", "--format", "{{.Created}}", id),
        Duration.ofSeconds(5)
    );
    if (result.exitCode() != 0) {
      return null;
    }
    try {
      return Instant.parse(result.output().trim());
    } catch (RuntimeException e) {
      return null;
    }
  }

  private void removeStaleDirectories() {
    if (!Files.isDirectory(workDir)) {
      return;
    }
    Instant cutoff = Instant.now().minus(STALE_AFTER);
    try (var children = Files.list(workDir)) {
      for (Path child : children.filter(Files::isDirectory).toList()) {
        if (!child.getFileName().toString().startsWith("run-")) {
          continue;
        }
        try {
          if (Files.getLastModifiedTime(child).toInstant().isBefore(cutoff)) {
            deleteRecursively(child);
            log.info("Removed stale runner directory {}", child);
          }
        } catch (IOException e) {
          log.warn("Could not inspect runner directory {}: {}", child, e.getMessage());
        }
      }
    } catch (IOException e) {
      log.warn("Could not list runner work directory {}: {}", workDir, e.getMessage());
    }
  }

  private CommandOutput runCommand(List<String> command, Duration timeout) {
    Process process = null;
    try {
      process = new ProcessBuilder(command).redirectErrorStream(true).start();
      StreamCollector output = new StreamCollector(process.getInputStream());
      boolean finished;
      try (ExecutorService streams = Executors.newVirtualThreadPerTaskExecutor()) {
        Future<?> task = streams.submit(output);
        finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
          process.destroyForcibly();
          closeQuietly(process.getInputStream());
          process.waitFor(2, TimeUnit.SECONDS);
        }
        awaitCollector(task);
      }
      return new CommandOutput(finished ? process.exitValue() : 124, output.text());
    } catch (IOException e) {
      return new CommandOutput(-1, e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      if (process != null) {
        process.destroyForcibly();
      }
      return new CommandOutput(-1, "Interrupted");
    }
  }

  private void deleteRecursively(Path directory) {
    if (directory == null) {
      return;
    }
    try (var paths = Files.walk(directory)) {
      paths.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException ignored) {
          // Best effort: a stale directory will be retried by the periodic cleanup.
        }
      });
    } catch (IOException ignored) {
      // Best effort: a stale directory will be retried by the periodic cleanup.
    }
  }

  private void relaxPermissions(Path directory) {
    try {
      Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwxrwxrwx"));
    } catch (IOException | UnsupportedOperationException e) {
      log.debug("Could not set permissions on {}: {}", directory, e.getMessage());
    }
  }

  private void makeReadable(Path file) {
    try {
      Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-r--r--"));
    } catch (IOException | UnsupportedOperationException e) {
      log.debug("Could not set permissions on {}: {}", file, e.getMessage());
    }
  }

  private void awaitCollector(Future<?> task) {
    try {
      task.get(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      task.cancel(true);
    } catch (ExecutionException | TimeoutException e) {
      task.cancel(true);
    }
  }

  private void closeQuietly(InputStream stream) {
    try {
      stream.close();
    } catch (IOException ignored) {
      // Closing an already closed stream is harmless.
    }
  }

  private long elapsed(long start) {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
  }

  private static String joinOutput(String first, String second) {
    if (first == null || first.isBlank()) {
      return second == null ? "" : second;
    }
    if (second == null || second.isBlank()) {
      return first;
    }
    return first + "\n" + second;
  }

  private static String prepend(String prefix, String value) {
    return prefix + Optional.ofNullable(value).orElse("");
  }

  private static String concise(String value) {
    String normalized = Optional.ofNullable(value).orElse("").replace('\n', ' ').trim();
    return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
  }

  private record ContainerRun(
      int exitCode,
      String stdout,
      String stderr,
      boolean timedOut,
      boolean oomKilled
  ) {}

  private record CommandOutput(int exitCode, String output) {}

  private static final class StreamCollector implements Runnable {
    private final InputStream input;
    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private boolean truncated;

    private StreamCollector(InputStream input) {
      this.input = input;
    }

    @Override
    public void run() {
      byte[] buffer = new byte[8192];
      try (input) {
        int read;
        while ((read = input.read(buffer)) != -1) {
          int remaining = OUTPUT_LIMIT - captured.size();
          if (remaining > 0) {
            captured.write(buffer, 0, Math.min(remaining, read));
          }
          if (read > Math.max(remaining, 0)) {
            truncated = true;
          }
        }
      } catch (IOException ignored) {
        // The process may be destroyed after a timeout; retain what was captured.
      }
    }

    private String text() {
      String value = captured.toString(StandardCharsets.UTF_8);
      return truncated ? value + TRUNCATED_MARKER : value;
    }
  }
}
