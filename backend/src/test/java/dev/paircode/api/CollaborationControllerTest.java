package dev.paircode.api;

import org.junit.jupiter.api.Test;
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
}
