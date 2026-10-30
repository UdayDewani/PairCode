package dev.paircode.api;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubmissionServiceTest {
  private final RoomService rooms = mock(RoomService.class);
  private final SubmissionRepository submissions = mock(SubmissionRepository.class);
  private final TestCaseRepository cases = mock(TestCaseRepository.class);
  private final TestResultRepository results = mock(TestResultRepository.class);
  private final CodeExecutor executor = mock(CodeExecutor.class);
  private final RealtimeService realtime = mock(RealtimeService.class);
  private final SessionRepository sessions = mock(SessionRepository.class);
  private final SubmissionService service = new SubmissionService(
      rooms,
      submissions,
      cases,
      results,
      executor,
      realtime,
      Runnable::run,
      mock(ApplicationEventPublisher.class),
      sessions
  );

  @Test
  void evaluationAggregatesScoreSummaryRuntimeAndFirstFailure() {
    UUID roomId = UUID.randomUUID();
    UUID problemId = UUID.randomUUID();
    var room = room(roomId);
    when(rooms.room(roomId)).thenReturn(room);

    var submission = submission(roomId, problemId);
    when(submissions.findById(submission.id)).thenReturn(Optional.of(submission));
    when(results.findBySubmissionId(submission.id)).thenReturn(List.of());

    var first = testCase(problemId, 0, false, "1\n", "1\n");
    var second = testCase(problemId, 1, true, "2\n", "2\n");
    when(cases.findByProblemIdOrderByOrdinal(problemId)).thenReturn(List.of(first, second));

    PreparedExecution execution = mock(PreparedExecution.class);
    when(executor.prepare("PYTHON", "print(1)")).thenReturn(execution);
    when(execution.compile()).thenReturn(CompileResult.ok(4));
    when(execution.run("1\n")).thenReturn(new ExecutionResult(
        SubmissionStatus.SUCCESS, "1\n", "", null, 0, 20, 0
    ));
    when(execution.run("2\n")).thenReturn(new ExecutionResult(
        SubmissionStatus.SUCCESS, "99\n", "", null, 0, 35, 0
    ));

    service.executeNow(submission.id, true);

    assertEquals(SubmissionStatus.WRONG_ANSWER, submission.status);
    assertEquals(50, submission.score);
    assertEquals("1/2 tests passed", submission.stdout);
    assertEquals("", submission.stderr);
    assertEquals(0, submission.exitCode);
    assertEquals(35L, submission.executionTimeMs);
    verify(execution).close();
    verify(results, times(2)).save(any(TestResult.class));
  }

  @Test
  void runtimeErrorOutranksWrongAnswerAndKeepsFirstFailureDetails() {
    UUID roomId = UUID.randomUUID();
    UUID problemId = UUID.randomUUID();
    when(rooms.room(roomId)).thenReturn(room(roomId));

    var submission = submission(roomId, problemId);
    when(submissions.findById(submission.id)).thenReturn(Optional.of(submission));
    when(results.findBySubmissionId(submission.id)).thenReturn(List.of());

    var first = testCase(problemId, 0, false, "1\n", "1\n");
    var second = testCase(problemId, 1, true, "2\n", "2\n");
    when(cases.findByProblemIdOrderByOrdinal(problemId)).thenReturn(List.of(first, second));

    PreparedExecution execution = mock(PreparedExecution.class);
    when(executor.prepare("PYTHON", "print(1)")).thenReturn(execution);
    when(execution.compile()).thenReturn(CompileResult.ok(4));
    when(execution.run("1\n")).thenReturn(new ExecutionResult(
        SubmissionStatus.RUNTIME_ERROR, "", "boom", null, 1, 12, 0
    ));
    when(execution.run("2\n")).thenReturn(new ExecutionResult(
        SubmissionStatus.SUCCESS, "wrong", "", null, 0, 30, 0
    ));

    service.executeNow(submission.id, true);

    assertEquals(SubmissionStatus.RUNTIME_ERROR, submission.status);
    assertEquals(0, submission.score);
    assertEquals("0/2 tests passed", submission.stdout);
    assertEquals("boom", submission.stderr);
    assertEquals(1, submission.exitCode);
    assertEquals(30L, submission.executionTimeMs);
  }

  @Test
  void compileFailureSkipsTestsAndReportsCompilerMessage() {
    UUID roomId = UUID.randomUUID();
    UUID problemId = UUID.randomUUID();
    when(rooms.room(roomId)).thenReturn(room(roomId));

    var submission = submission(roomId, problemId);
    when(submissions.findById(submission.id)).thenReturn(Optional.of(submission));
    when(cases.findByProblemIdOrderByOrdinal(problemId)).thenReturn(List.of(
        testCase(problemId, 0, false, "1\n", "1\n")
    ));

    PreparedExecution execution = mock(PreparedExecution.class);
    when(executor.prepare("PYTHON", "print(1)")).thenReturn(execution);
    when(execution.compile()).thenReturn(CompileResult.failed("syntax error", 1, 7));

    service.executeNow(submission.id, true);

    assertEquals(SubmissionStatus.COMPILE_ERROR, submission.status);
    assertEquals("syntax error", submission.compileError);
    assertNull(submission.stderr, "Compiler output must not be duplicated into stderr");
    assertEquals("", submission.stdout);
    assertEquals(0, submission.score);
    assertEquals(1, submission.exitCode);
    assertEquals(7L, submission.executionTimeMs);
    verify(execution, never()).run(any());
    verify(results, never()).save(any(TestResult.class));
  }

  @Test
  void startupMarksInterruptedSubmissionsAsFailed() {
    var interrupted = submission(UUID.randomUUID(), UUID.randomUUID());
    interrupted.status = SubmissionStatus.RUNNING;
    var queued = submission(UUID.randomUUID(), UUID.randomUUID());
    queued.status = SubmissionStatus.QUEUED;
    when(submissions.findByStatusIn(List.of(SubmissionStatus.QUEUED, SubmissionStatus.RUNNING)))
        .thenReturn(List.of(interrupted, queued));

    service.failInterruptedSubmissions();

    assertEquals(SubmissionStatus.RUNTIME_ERROR, interrupted.status);
    assertEquals(SubmissionStatus.RUNTIME_ERROR, queued.status);
    assertTrue(interrupted.stderr.contains("server restart"));
    verify(submissions).saveAll(List.of(interrupted, queued));
  }

  private static Room room(UUID roomId) {
    var room = new Room();
    room.id = roomId;
    room.roomCode = "PC-TEST1";
    return room;
  }

  private static Submission submission(UUID roomId, UUID problemId) {
    var submission = new Submission();
    submission.id = UUID.randomUUID();
    submission.roomId = roomId;
    submission.problemId = problemId;
    submission.userId = UUID.randomUUID();
    submission.language = "PYTHON";
    submission.sourceCode = "print(1)";
    submission.status = SubmissionStatus.QUEUED;
    return submission;
  }

  private static TestCase testCase(UUID problemId, int ordinal, boolean hidden, String input, String expectedOutput) {
    var testCase = new TestCase();
    testCase.id = UUID.randomUUID();
    testCase.problemId = problemId;
    testCase.ordinal = ordinal;
    testCase.hidden = hidden;
    testCase.input = input;
    testCase.expectedOutput = expectedOutput;
    return testCase;
  }
}
