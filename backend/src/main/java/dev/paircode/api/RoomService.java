package dev.paircode.api;

import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service
class RoomService {
  private static final char[] CODE="ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray(); private final SecureRandom random=new SecureRandom();
  private final RoomRepository rooms; private final ParticipantRepository participants; private final UserRepository users; private final ProblemRepository problems; private final SessionRepository sessions; private final SnapshotRepository snapshots;
  RoomService(RoomRepository rooms,ParticipantRepository participants,UserRepository users,ProblemRepository problems,SessionRepository sessions,SnapshotRepository snapshots){this.rooms=rooms;this.participants=participants;this.users=users;this.problems=problems;this.sessions=sessions;this.snapshots=snapshots;}
  @Transactional Room create(UUID owner,String name,UUID problemId,String language,boolean candidateCanEdit){
    if(problemId!=null&&!problems.existsById(problemId))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Problem not found");var r=new Room();r.roomCode=code();r.name=name;r.ownerId=owner;r.problemId=problemId;r.language=language==null?"PYTHON":language;r.candidateCanEdit=candidateCanEdit;rooms.save(r);participants.save(new RoomParticipant(r.id,owner,Role.INTERVIEWER));sessions.save(new SessionRecord(r.id,owner));return r;
  }
  @Transactional Room join(UUID user,String roomCode){var room=byCode(roomCode);if(room.status==RoomStatus.ENDED)throw new ResponseStatusException(HttpStatus.GONE,"Session has ended");if(participants.findByRoomIdAndUserId(room.id,user).isEmpty())participants.save(new RoomParticipant(room.id,user,Role.CANDIDATE));return room;}
  Room byCode(String code){return rooms.findByRoomCode(code.toUpperCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Room not found"));}
  Room room(UUID room){return rooms.findById(room).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Room not found"));}
  RoomParticipant member(UUID room,UUID user){return participants.findByRoomIdAndUserId(room,user).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"You are not a room participant"));}
  void interviewer(UUID room,UUID user){if(member(room,user).role!=Role.INTERVIEWER)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Interviewer role required");}
  void canEdit(UUID room,UUID user){var r=room(room);var m=member(room,user);if(m.role==Role.CANDIDATE&&(!r.candidateCanEdit||r.editorLocked))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Editor is locked for candidates");}
  @Transactional Room permission(UUID roomId,UUID user,Boolean candidateCanEdit,Boolean editorLocked){interviewer(roomId,user);var r=room(roomId);if(candidateCanEdit!=null)r.candidateCanEdit=candidateCanEdit;if(editorLocked!=null)r.editorLocked=editorLocked;return rooms.save(r);}
  @Transactional Room changeProblem(UUID roomId,UUID user,UUID problemId){interviewer(roomId,user);if(!problems.existsById(problemId))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Problem not found");var r=room(roomId);r.problemId=problemId;return rooms.save(r);}
  @Transactional void removeParticipant(UUID roomId,UUID interviewer,UUID userId){interviewer(roomId,interviewer);if(room(roomId).ownerId.equals(userId))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The interviewer cannot be removed");var member=member(roomId,userId);member.leftAt=Instant.now();participants.save(member);}
  @Transactional void end(UUID roomId,UUID user,String document){interviewer(roomId,user);var r=room(roomId);if(r.status==RoomStatus.ENDED)return;r.status=RoomStatus.ENDED;r.endedAt=Instant.now();rooms.save(r);if(document!=null)snapshots.save(new CodeSnapshot(r.id,user,document,"SESSION_END"));sessions.findByRoomId(r.id).ifPresent(s->{s.endedAt=r.endedAt;s.durationSeconds=Duration.between(s.startedAt,s.endedAt).toSeconds();sessions.save(s);});}
  @Transactional void snapshot(UUID room,UUID user,String source,String reason){member(room,user);snapshots.save(new CodeSnapshot(room,user,source,reason));}
  List<Map<String,Object>> participants(UUID room){return participants.findByRoomId(room).stream().<Map<String,Object>>map(p->{var u=users.findById(p.userId).orElseThrow();Map<String,Object> m=new LinkedHashMap<>();m.put("id",u.id);m.put("name",u.displayName);m.put("email",u.email);m.put("role",p.role);m.put("online",p.leftAt==null);return m;}).toList();}
  Map<String,Object> dto(Room r){var m=new LinkedHashMap<String,Object>();m.put("id",r.id);m.put("roomId",r.roomCode);m.put("name",r.name);m.put("status",r.status);m.put("language",r.language);m.put("candidateCanEdit",r.candidateCanEdit);m.put("editorLocked",r.editorLocked);m.put("documentState",r.documentState==null?null:Base64.getEncoder().encodeToString(r.documentState));m.put("createdAt",r.createdAt);m.put("endedAt",r.endedAt);m.put("problem",r.problemId==null?null:problems.findById(r.problemId).map(Api::problem).orElse(null));m.put("participants",participants(r.id));return m;}
  private String code(){String value;do{var b=new StringBuilder("PC-");for(int i=0;i<5;i++)b.append(CODE[random.nextInt(CODE.length)]);value=b.toString();}while(rooms.findByRoomCode(value).isPresent());return value;}
}
