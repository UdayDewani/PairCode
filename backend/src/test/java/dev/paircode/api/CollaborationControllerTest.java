package dev.paircode.api;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CollaborationControllerTest {
  @Test void runSucceedsAfterRoomLanguageChanges() {
    var rooms=mock(RoomService.class);
    var submissions=mock(SubmissionService.class);
    var controller=new CollaborationController(
      rooms,
      mock(ChatRepository.class),
      mock(UserRepository.class),
      submissions,
      mock(RealtimeService.class),
      new SimpleRateLimiter()
    );
    UUID roomId=UUID.randomUUID(), userId=UUID.randomUUID();
    var room=new Room(); room.id=roomId; room.roomCode="PC-JAVA1"; room.language="JAVA";
    var submission=new Submission();
    when(rooms.byCode("PC-JAVA1")).thenReturn(room);
    when(submissions.enqueue(roomId,userId,"class Main {}","JAVA",null,false)).thenReturn(submission);
    when(submissions.dto(submission)).thenReturn(Map.of("id",submission.id));

    var response=controller.run(new AuthPrincipal(userId),"PC-JAVA1",new RunRequest("class Main {}","JAVA",null));

    assertEquals(202,response.getStatusCode().value());
    verify(submissions).enqueue(roomId,userId,"class Main {}","JAVA",null,false);
  }

  @Test void submissionEndpointStripsHiddenTestData() {
    var rooms=mock(RoomService.class);
    var submissionRepository=mock(SubmissionRepository.class);
    var testCaseRepository=mock(TestCaseRepository.class);
    var testResultRepository=mock(TestResultRepository.class);
    var submissions=new SubmissionService(
      rooms,
      submissionRepository,
      testCaseRepository,
      testResultRepository,
      mock(CodeExecutor.class),
      mock(RealtimeService.class),
      Runnable::run,
      mock(ApplicationEventPublisher.class),
      mock(SessionRepository.class)
    );
    var controller=new CollaborationController(
      rooms,
      mock(ChatRepository.class),
      mock(UserRepository.class),
      submissions,
      mock(RealtimeService.class),
      new SimpleRateLimiter()
    );
    UUID roomId=UUID.randomUUID(), userId=UUID.randomUUID(), submissionId=UUID.randomUUID();
    var room=new Room(); room.id=roomId; room.roomCode="PC-HIDDEN";
    var submission=new Submission(); submission.id=submissionId; submission.roomId=roomId; submission.status=SubmissionStatus.WRONG_ANSWER;
    when(rooms.byCode("PC-HIDDEN")).thenReturn(room);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

    var visible=testCase(roomId,false,0,"2 7 11 15\n9\n","0 1\n");
    var hidden=testCase(roomId,true,1,"3 2 4\n6\n","1 2\n");
    when(testCaseRepository.findById(visible.id)).thenReturn(Optional.of(visible));
    when(testCaseRepository.findById(hidden.id)).thenReturn(Optional.of(hidden));
    when(testResultRepository.findBySubmissionId(submissionId)).thenReturn(List.of(
      new TestResult(submissionId,hidden.id,false,12,"0 0\n"),
      new TestResult(submissionId,visible.id,false,8,"0 0\n")
    ));

    var response=controller.submission(new AuthPrincipal(userId),"PC-HIDDEN",submissionId);

    @SuppressWarnings("unchecked")
    var results=(List<Map<String,Object>>)response.get("testResults");
    assertEquals(2,results.size());
    assertEquals(List.of(visible.id,hidden.id),results.stream().map(result->result.get("testCaseId")).toList());

    assertEquals("2 7 11 15\n9\n",results.get(0).get("input"));
    assertEquals("0 1\n",results.get(0).get("expectedOutput"));
    assertEquals("0 0\n",results.get(0).get("actualOutput"));

    var hiddenResult=results.get(1);
    assertEquals(hidden.id,hiddenResult.get("testCaseId"));
    assertEquals(false,hiddenResult.get("passed"));
    assertEquals(12L,hiddenResult.get("runtimeMs"));
    assertFalse(hiddenResult.containsKey("input"),"Hidden test input must not be exposed");
    assertFalse(hiddenResult.containsKey("expectedOutput"),"Hidden expected output must not be exposed");
    assertFalse(hiddenResult.containsKey("actualOutput"),"Hidden actual output must not be exposed");
    verify(rooms).member(roomId,userId);
  }

  private static TestCase testCase(UUID problemId,boolean hidden,int ordinal,String input,String expectedOutput) {
    var testCase=new TestCase();
    testCase.id=UUID.randomUUID();
    testCase.problemId=problemId;
    testCase.hidden=hidden;
    testCase.ordinal=ordinal;
    testCase.input=input;
    testCase.expectedOutput=expectedOutput;
    return testCase;
  }
}
