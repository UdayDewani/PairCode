package dev.paircode.api;

import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@RestController @RequestMapping("/api/v1")
class SessionController {
  private final SessionRepository sessions; private final RoomRepository rooms; private final RoomService roomService; private final SubmissionRepository submissions; private final SubmissionService submissionService; private final SnapshotRepository snapshots; private final ChatRepository chat; private final NoteRepository notes; private final UserRepository users;
  SessionController(SessionRepository sessions,RoomRepository rooms,RoomService roomService,SubmissionRepository submissions,SubmissionService submissionService,SnapshotRepository snapshots,ChatRepository chat,NoteRepository notes,UserRepository users){this.sessions=sessions;this.rooms=rooms;this.roomService=roomService;this.submissions=submissions;this.submissionService=submissionService;this.snapshots=snapshots;this.chat=chat;this.notes=notes;this.users=users;}
  @GetMapping("/sessions") List<Map<String,Object>> list(@AuthenticationPrincipal AuthPrincipal p){UUID user=Api.user(p);return sessions.findByOwnerIdOrderByStartedAtDesc(user).stream().map(this::summary).toList();}
  @GetMapping("/sessions/{id}") Map<String,Object> review(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){var s=sessions.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Session not found"));if(!s.ownerId.equals(Api.user(p)))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the session owner may review this session");var room=rooms.findById(s.roomId).orElseThrow();var m=summary(s);m.put("room",roomService.dto(room));m.put("snapshots",snapshots.findByRoomIdOrderByCreatedAtAsc(room.id).stream().map(x->Map.of("id",x.id,"sourceCode",x.sourceCode,"reason",x.reason,"createdAt",x.createdAt)).toList());m.put("submissions",submissions.findByRoomIdOrderByCreatedAtDesc(room.id).stream().map(submissionService::dto).toList());m.put("chatMessages",chat.findByRoomIdOrderByCreatedAtAsc(room.id).stream().map(x->Map.of("id",x.id,"content",x.content,"type",x.type,"createdAt",x.createdAt)).toList());m.put("notes",notes.findBySessionIdOrderByCreatedAtAsc(s.id).stream().map(x->Map.of("id",x.id,"content",x.content,"createdAt",x.createdAt)).toList());return m;}
  @GetMapping("/dashboard") Map<String,Object> dashboard(@AuthenticationPrincipal AuthPrincipal p){var list=sessions.findByOwnerIdOrderByStartedAtDesc(Api.user(p));long completed=list.stream().filter(x->x.endedAt!=null).count();return Map.of("totalSessions",list.size(),"completedSessions",completed,"recentSessions",list.stream().limit(5).map(this::summary).toList());}
  private Map<String,Object> summary(SessionRecord s){var room=rooms.findById(s.roomId).orElseThrow();var m=new LinkedHashMap<String,Object>();m.put("id",s.id);m.put("roomId",room.roomCode);m.put("name",room.name);m.put("status",room.status);m.put("language",room.language);m.put("score",s.score);m.put("startedAt",s.startedAt);m.put("endedAt",s.endedAt);m.put("durationSeconds",s.durationSeconds);return m;}
}
