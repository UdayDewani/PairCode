package dev.paircode.api;

import java.util.List;

/**
 * Pure evaluation rules shared by the judge and its unit tests.
 */
final class Evaluation {
  private Evaluation() {}

  /**
   * A successful process with the wrong output is a wrong answer. Any other process status
   * keeps its own failure status.
   */
  static SubmissionStatus testStatus(ExecutionResult result, boolean outputMatches) {
    if (result.status() == SubmissionStatus.SUCCESS) {
      return outputMatches ? SubmissionStatus.SUCCESS : SubmissionStatus.WRONG_ANSWER;
    }
    return result.status();
  }

  /**
   * Status priority for an aggregated submission: COMPILE_ERROR, RUNTIME_ERROR,
   * TIME_LIMIT_EXCEEDED, WRONG_ANSWER, SUCCESS.
   */
  static SubmissionStatus overallStatus(List<SubmissionStatus> failureStatuses) {
    boolean compileError = false;
    boolean runtimeError = false;
    boolean timeLimitExceeded = false;
    boolean wrongAnswer = false;

    for (SubmissionStatus status : failureStatuses) {
      switch (status) {
        case COMPILE_ERROR -> compileError = true;
        case RUNTIME_ERROR -> runtimeError = true;
        case TIME_LIMIT_EXCEEDED -> timeLimitExceeded = true;
        case WRONG_ANSWER -> wrongAnswer = true;
        default -> {
          // Passing tests and queued/running states do not affect the aggregate.
        }
      }
    }

    if (compileError) {
      return SubmissionStatus.COMPILE_ERROR;
    }
    if (runtimeError) {
      return SubmissionStatus.RUNTIME_ERROR;
    }
    if (timeLimitExceeded) {
      return SubmissionStatus.TIME_LIMIT_EXCEEDED;
    }
    if (wrongAnswer) {
      return SubmissionStatus.WRONG_ANSWER;
    }
    return SubmissionStatus.SUCCESS;
  }
}
