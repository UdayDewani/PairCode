package dev.paircode.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RunnerHealthIndicatorTest {
  @Test
  void missingDockerMakesTheRunnerHealthDown() {
    RunnerHealthIndicator indicator = new RunnerHealthIndicator(true, "paircode-missing-docker-command");

    RunnerHealthIndicator.CheckResult result = indicator.checkNow();

    assertFalse(result.healthy());
    assertEquals(Status.DOWN, indicator.health().getStatus());
    assertTrue(indicator.health().getDetails().containsKey("error"));
  }

  @Test
  void missingImageMakesTheRunnerHealthDown() {
    assumeTrue(dockerAvailable(), "Docker CLI/daemon is not available");
    String missingImage = "paircode-runner-does-not-exist";
    RunnerHealthIndicator indicator = new RunnerHealthIndicator(true, "docker", List.of(missingImage));

    RunnerHealthIndicator.CheckResult result = indicator.checkNow();

    assertFalse(result.healthy());
    assertTrue(result.missingImages().contains(missingImage));
    assertEquals(Status.DOWN, indicator.health().getStatus());
    assertEquals(List.of(missingImage), indicator.health().getDetails().get("missingImages"));
  }

  @Test
  void disabledRunnerIsHealthy() {
    RunnerHealthIndicator indicator = new RunnerHealthIndicator(false, "docker");

    assertEquals(Status.UP, indicator.health().getStatus());
    assertEquals(false, indicator.health().getDetails().get("enabled"));
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
}
