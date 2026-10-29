package dev.paircode.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component("runner")
class RunnerHealthIndicator implements HealthIndicator {
  private static final Logger log = LoggerFactory.getLogger(RunnerHealthIndicator.class);
  private static final int OUTPUT_LIMIT = 8 * 1024;

  private final boolean enabled;
  private final String docker;
  private final List<String> images;
  private volatile CheckResult lastResult;

  @Autowired
  RunnerHealthIndicator(
      @Value("${app.runner-enabled}") boolean enabled,
      @Value("${app.docker-command}") String docker
  ) {
    this(enabled, docker, RunnerImages.all());
  }

  RunnerHealthIndicator(boolean enabled, String docker, List<String> images) {
    this.enabled = enabled;
    this.docker = docker;
    this.images = List.copyOf(images);
  }

  @EventListener(ApplicationReadyEvent.class)
  void checkOnStartup() {
    if (enabled) {
      checkNow();
    }
  }

  @Scheduled(initialDelay = 300_000, fixedDelay = 300_000)
  void recheckPeriodically() {
    if (enabled) {
      checkNow();
    }
  }

  synchronized CheckResult checkNow() {
    if (!enabled) {
      CheckResult disabled = new CheckResult(false, "", List.of(), "Runner is disabled.", Instant.now());
      lastResult = disabled;
      return disabled;
    }

    CommandOutput version = runCommand(
        List.of(docker, "version", "--format", "{{.Server.Version}}"),
        Duration.ofSeconds(10)
    );
    if (version.exitCode() != 0) {
      String error = concise(version.output());
      log.error(
          "Runner is enabled but Docker is unavailable. Check that the Docker CLI and socket are usable: {}",
          error
      );
      CheckResult result = new CheckResult(false, "", List.of(), error, Instant.now());
      lastResult = result;
      return result;
    }

    String dockerVersion = version.output().trim();
    List<String> missingImages = new ArrayList<>();
    for (String image : images) {
      CommandOutput inspect = runCommand(
          List.of(docker, "image", "inspect", "--format", "{{.Id}}", image),
          Duration.ofSeconds(10)
      );
      if (inspect.exitCode() != 0) {
        missingImages.add(image);
        log.error("Runner image {} is missing. Build it with: {}", image, RunnerImages.buildCommand(image));
      }
    }

    if (missingImages.isEmpty()) {
      log.info("Runner is ready: Docker {} and all runner images are present.", dockerVersion);
    }

    CheckResult result = new CheckResult(true, dockerVersion, List.copyOf(missingImages), null, Instant.now());
    lastResult = result;
    return result;
  }

  @Override
  public Health health() {
    if (!enabled) {
      return Health.up()
          .withDetail("enabled", false)
          .withDetail("message", "Runner is disabled.")
          .build();
    }

    CheckResult result = lastResult;
    if (result == null) {
      return Health.unknown()
          .withDetail("enabled", true)
          .withDetail("message", "Runner self-check is still running.")
          .build();
    }

    Health.Builder builder = result.healthy() ? Health.up() : Health.down();
    builder.withDetail("enabled", true)
        .withDetail("checkedAt", result.checkedAt().toString());
    if (!result.dockerVersion().isBlank()) {
      builder.withDetail("dockerVersion", result.dockerVersion());
    }
    if (!result.missingImages().isEmpty()) {
      builder.withDetail("missingImages", result.missingImages());
    }
    if (result.error() != null) {
      builder.withDetail("error", result.error());
    }
    return builder.build();
  }

  private CommandOutput runCommand(List<String> command, Duration timeout) {
    Process process = null;
    try {
      process = new ProcessBuilder(command).redirectErrorStream(true).start();
      OutputCollector collector = new OutputCollector(process.getInputStream());
      boolean finished;
      try (ExecutorService streams = Executors.newVirtualThreadPerTaskExecutor()) {
        Future<?> task = streams.submit(collector);
        finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
          process.destroyForcibly();
          closeQuietly(process.getInputStream());
          process.waitFor(2, TimeUnit.SECONDS);
        }
        await(task);
      }
      return new CommandOutput(finished ? process.exitValue() : 124, collector.text());
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

  private void await(Future<?> task) {
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

  private String concise(String value) {
    String normalized = Optional.ofNullable(value).orElse("").replace('\n', ' ').trim();
    return normalized.length() > 500 ? normalized.substring(0, 500) + "..." : normalized;
  }

  record CheckResult(
      boolean dockerAvailable,
      String dockerVersion,
      List<String> missingImages,
      String error,
      Instant checkedAt
  ) {
    boolean healthy() {
      return dockerAvailable && missingImages.isEmpty();
    }
  }

  private record CommandOutput(int exitCode, String output) {}

  private static final class OutputCollector implements Runnable {
    private final InputStream input;
    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    private OutputCollector(InputStream input) {
      this.input = input;
    }

    @Override
    public void run() {
      byte[] buffer = new byte[4096];
      try (input) {
        int read;
        while ((read = input.read(buffer)) != -1) {
          int remaining = OUTPUT_LIMIT - captured.size();
          if (remaining > 0) {
            captured.write(buffer, 0, Math.min(remaining, read));
          }
        }
      } catch (IOException ignored) {
        // The process may be destroyed after a timeout.
      }
    }

    private String text() {
      return captured.toString(StandardCharsets.UTF_8);
    }
  }
}
