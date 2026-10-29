package dev.paircode.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {
  @Test void lockedCandidateCannotEdit() {
    var rooms=mock(RoomRepository.class); var participants=mock(ParticipantRepository.class);
    var service=new RoomService(rooms,participants,mock(UserRepository.class),mock(ProblemRepository.class),mock(SessionRepository.class),mock(SnapshotRepository.class));
    UUID roomId=UUID.randomUUID(), candidate=UUID.randomUUID(); var room=new Room(); room.id=roomId; room.editorLocked=true;
    var member=new RoomParticipant(roomId,candidate,Role.CANDIDATE);
    when(rooms.findById(roomId)).thenReturn(Optional.of(room)); when(participants.findByRoomIdAndUserId(roomId,candidate)).thenReturn(Optional.of(member));
    var error=assertThrows(ResponseStatusException.class,()->service.canEdit(roomId,candidate));
    assertEquals(HttpStatus.FORBIDDEN,error.getStatusCode());
  }
  @Test void interviewerCanEditWhileRoomLocked() {
    var rooms=mock(RoomRepository.class); var participants=mock(ParticipantRepository.class);
    var service=new RoomService(rooms,participants,mock(UserRepository.class),mock(ProblemRepository.class),mock(SessionRepository.class),mock(SnapshotRepository.class));
    UUID roomId=UUID.randomUUID(), interviewer=UUID.randomUUID(); var room=new Room();room.id=roomId;room.editorLocked=true;
    when(rooms.findById(roomId)).thenReturn(Optional.of(room));when(participants.findByRoomIdAndUserId(roomId,interviewer)).thenReturn(Optional.of(new RoomParticipant(roomId,interviewer,Role.INTERVIEWER)));
    assertDoesNotThrow(()->service.canEdit(roomId,interviewer));
  }

  @Test void interviewerCanChangeLanguage() {
    var rooms=mock(RoomRepository.class); var participants=mock(ParticipantRepository.class);
    var service=new RoomService(rooms,participants,mock(UserRepository.class),mock(ProblemRepository.class),mock(SessionRepository.class),mock(SnapshotRepository.class));
    UUID roomId=UUID.randomUUID(), interviewer=UUID.randomUUID(); var room=new Room(); room.id=roomId;
    when(rooms.findById(roomId)).thenReturn(Optional.of(room));
    when(participants.findByRoomIdAndUserId(roomId,interviewer)).thenReturn(Optional.of(new RoomParticipant(roomId,interviewer,Role.INTERVIEWER)));
    when(rooms.save(room)).thenReturn(room);

    assertEquals("JAVA",service.changeLanguage(roomId,interviewer,"JAVA").language);
    verify(rooms).save(room);
  }

  @Test void lockedCandidateCannotChangeLanguage() {
    var rooms=mock(RoomRepository.class); var participants=mock(ParticipantRepository.class);
    var service=new RoomService(rooms,participants,mock(UserRepository.class),mock(ProblemRepository.class),mock(SessionRepository.class),mock(SnapshotRepository.class));
    UUID roomId=UUID.randomUUID(), candidate=UUID.randomUUID(); var room=new Room(); room.id=roomId; room.editorLocked=true;
    when(rooms.findById(roomId)).thenReturn(Optional.of(room));
    when(participants.findByRoomIdAndUserId(roomId,candidate)).thenReturn(Optional.of(new RoomParticipant(roomId,candidate,Role.CANDIDATE)));

    var error=assertThrows(ResponseStatusException.class,()->service.changeLanguage(roomId,candidate,"JAVA"));
    assertEquals(HttpStatus.FORBIDDEN,error.getStatusCode());
  }

  @Test void endedRoomCannotChangeLanguage() {
    var rooms=mock(RoomRepository.class); var participants=mock(ParticipantRepository.class);
    var service=new RoomService(rooms,participants,mock(UserRepository.class),mock(ProblemRepository.class),mock(SessionRepository.class),mock(SnapshotRepository.class));
    UUID roomId=UUID.randomUUID(); var room=new Room(); room.id=roomId; room.status=RoomStatus.ENDED;
    when(rooms.findById(roomId)).thenReturn(Optional.of(room));

    var error=assertThrows(ResponseStatusException.class,()->service.changeLanguage(roomId,UUID.randomUUID(),"JAVA"));
    assertEquals(HttpStatus.GONE,error.getStatusCode());
  }
}
