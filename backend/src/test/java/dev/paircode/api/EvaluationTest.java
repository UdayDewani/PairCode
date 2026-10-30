package dev.paircode.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluationTest {
  @Test
  void successfulProcessWithWrongOutputBecomesWrongAnswer() {
    ExecutionResult result = new ExecutionResult(
        SubmissionStatus.SUCCESS,
        "wrong output",
        "",
        null,
        0,
        12,
        0
    );

    assertEquals(SubmissionStatus.SUCCESS, Evaluation.testStatus(result, true));
    assertEquals(SubmissionStatus.WRONG_ANSWER, Evaluation.testStatus(result, false));
  }

  @Test
  void processFailuresKeepTheirStatusEvenWhenOutputIsEmpty() {
    assertEquals(
        SubmissionStatus.RUNTIME_ERROR,
        Evaluation.testStatus(new ExecutionResult(SubmissionStatus.RUNTIME_ERROR, "", "boom", null, 1, 10, 0), false)
    );
    assertEquals(
        SubmissionStatus.TIME_LIMIT_EXCEEDED,
        Evaluation.testStatus(new ExecutionResult(SubmissionStatus.TIME_LIMIT_EXCEEDED, "", "timeout", null, 124, 8000, 0), false)
    );
    assertEquals(
        SubmissionStatus.COMPILE_ERROR,
        Evaluation.testStatus(new ExecutionResult(SubmissionStatus.COMPILE_ERROR, "", "", "syntax error", 1, 30, 0), false)
    );
  }

  @Test
  void overallStatusUsesDocumentedPriority() {
    assertEquals(
        SubmissionStatus.COMPILE_ERROR,
        Evaluation.overallStatus(List.of(
            SubmissionStatus.WRONG_ANSWER,
            SubmissionStatus.TIME_LIMIT_EXCEEDED,
            SubmissionStatus.RUNTIME_ERROR,
            SubmissionStatus.COMPILE_ERROR
        ))
    );
    assertEquals(
        SubmissionStatus.RUNTIME_ERROR,
        Evaluation.overallStatus(List.of(
            SubmissionStatus.WRONG_ANSWER,
            SubmissionStatus.TIME_LIMIT_EXCEEDED,
            SubmissionStatus.RUNTIME_ERROR
        ))
    );
    assertEquals(
        SubmissionStatus.TIME_LIMIT_EXCEEDED,
        Evaluation.overallStatus(List.of(
            SubmissionStatus.WRONG_ANSWER,
            SubmissionStatus.TIME_LIMIT_EXCEEDED
        ))
    );
    assertEquals(
        SubmissionStatus.WRONG_ANSWER,
        Evaluation.overallStatus(List.of(SubmissionStatus.WRONG_ANSWER))
    );
    assertEquals(SubmissionStatus.SUCCESS, Evaluation.overallStatus(List.of()));
    assertEquals(
        SubmissionStatus.SUCCESS,
        Evaluation.overallStatus(List.of(SubmissionStatus.SUCCESS, SubmissionStatus.SUCCESS))
    );
  }
}
