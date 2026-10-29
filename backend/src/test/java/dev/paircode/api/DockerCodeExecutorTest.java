package dev.paircode.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DockerCodeExecutorTest {
  @TempDir
  static Path tempDirectory;

  private static DockerCodeExecutor executor;

  @BeforeAll
  static void setUp() {
    assumeTrue(dockerAvailable(), "Docker CLI/daemon is not available");
    assumeTrue(
        runnerImagesAvailable(),
        "Runner images are missing. Build them with: " + String.join(" && ", RunnerImages.all().stream()
            .map(RunnerImages::buildCommand)
            .toList())
    );

    Path workDirectory = tempDirectory.resolve("runs");
    try {
      Files.createDirectories(workDirectory);
    } catch (IOException e) {
      throw new IllegalStateException("Could not create the test runner work directory", e);
    }
    executor = new DockerCodeExecutor(true, "docker", workDirectory.toString());
  }

  @Test
  void pythonPrintsTwo() {
    ExecutionResult result = executor.execute(new ExecutionRequest("PYTHON", "print(1+1)", ""));

    assertEquals(SubmissionStatus.SUCCESS, result.status(), () -> failureMessage(result));
    assertEquals("2", result.stdout().trim());
  }

  @Test
  void javaPrintsTwo() {
    String source = """
        public class Main {
          public static void main(String[] args) {
            System.out.println(1 + 1);
          }
        }
        """;

    ExecutionResult result = executor.execute(new ExecutionRequest("JAVA", source, ""));

    assertEquals(SubmissionStatus.SUCCESS, result.status(), () -> failureMessage(result));
    assertEquals("2", result.stdout().trim());
  }

  @Test
  void cppPrintsTwo() {
    String source = """
        #include <iostream>

        int main() {
          std::cout << (1 + 1) << std::endl;
          return 0;
        }
        """;

    ExecutionResult result = executor.execute(new ExecutionRequest("CPP", source, ""));

    assertEquals(SubmissionStatus.SUCCESS, result.status(), () -> failureMessage(result));
    assertEquals("2", result.stdout().trim());
  }

  @Test
  void javaCompileErrorIsReported() {
    String source = """
        public class Main {
          public static void main(String[] args) {
            int broken = ;
          }
        }
        """;

    ExecutionResult result = executor.execute(new ExecutionRequest("JAVA", source, ""));

    assertEquals(SubmissionStatus.COMPILE_ERROR, result.status());
    assertFalse(result.compileError().isBlank());
  }

  @Test
  void infiniteLoopTimesOut() {
    String source = """
        while True:
            pass
        """;

    long started = System.nanoTime();
    ExecutionResult result = executor.execute(new ExecutionRequest("PYTHON", source, ""));
    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

    assertEquals(SubmissionStatus.TIME_LIMIT_EXCEEDED, result.status(), () -> failureMessage(result));
    assertTrue(elapsedMs < 12_000, "Expected a timeout in about 8 seconds, took " + elapsedMs + " ms");
  }

  @Test
  void largeOutputIsTruncatedWithoutHanging() {
    String source = "print('x' * 1_000_000)";

    long started = System.nanoTime();
    ExecutionResult result = executor.execute(new ExecutionRequest("PYTHON", source, ""));
    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

    assertEquals(SubmissionStatus.SUCCESS, result.status(), () -> failureMessage(result));
    assertTrue(result.stdout().endsWith(DockerCodeExecutor.TRUNCATED_MARKER));
    assertTrue(
        result.stdout().length() <= DockerCodeExecutor.OUTPUT_LIMIT + DockerCodeExecutor.TRUNCATED_MARKER.length(),
        "Captured output should stop at the configured limit"
    );
    assertTrue(elapsedMs < 12_000, "Large output should be drained, took " + elapsedMs + " ms");
  }

  private static String failureMessage(ExecutionResult result) {
    return "status=" + result.status()
        + "\nstdout=" + result.stdout()
        + "\nstderr=" + result.stderr()
        + "\ncompileError=" + result.compileError();
  }

  private static boolean dockerAvailable() {
    try {
      Process process = new ProcessBuilder(
          "docker", "version", "--format", "{{.Server.Version}}"
      ).redirectErrorStream(true).start();
      process.getInputStream().readAllBytes();
      return process.waitFor(15, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private static boolean runnerImagesAvailable() {
    try {
      for (String image : RunnerImages.all()) {
        Process process = new ProcessBuilder(
            "docker", "image", "inspect", "--format", "{{.Id}}", image
        ).redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        if (!process.waitFor(15, TimeUnit.SECONDS) || process.exitValue() != 0) {
          return false;
        }
      }
      return true;
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
