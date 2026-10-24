package dev.paircode.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

record ChatRequest(@NotBlank @Size(max=4000) String content) {}
record RunRequest(@NotBlank String sourceCode,@Pattern(regexp="JAVA|PYTHON|CPP") String language,String stdin) {}
@RestController @RequestMapping("/api/v1/rooms/{roomCode}")
class CollaborationController {
  private final RoomService rooms; private final ChatRepository chat; private final UserRepository users; private final SubmissionService submissions; private final RealtimeService realtime; private final SimpleRateLimiter limiter;
  CollaborationController(RoomService rooms,ChatRepository chat,UserRepository users,SubmissionService submissions,RealtimeService realtime,SimpleRateLimiter limiter){this.rooms=rooms;this.chat=chat;this.users=users;this.submissions=submissions;this.realtime=realtime;this.limiter=limiter;}
  @GetMapping("/messages") List<Map<String,Object>> messages(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode){var r=rooms.byCode(roomCode);rooms.member(r.id,Api.user(p));return chat.findByRoomIdOrderByCreatedAtAsc(r.id).stream().map(this::message).toList();}
  @PostMapping("/messages") ResponseEntity<Map<String,Object>> message(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@Valid @RequestBody ChatRequest request){var r=rooms.byCode(roomCode);UUID user=Api.user(p);rooms.member(r.id,user);var result=message(chat.save(new ChatMessage(r.id,user,request.content(),"CHAT")));realtime.event(r.roomCode,"chat",result);return ResponseEntity.status(HttpStatus.CREATED).body(result);}
  @PostMapping("/run") ResponseEntity<Map<String,Object>> run(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@Valid @RequestBody RunRequest request){UUID user=Api.user(p);limiter.check("run:"+user,20);var r=rooms.byCode(roomCode);var s=submissions.enqueue(r.id,user,request.sourceCode(),request.language(),request.stdin(),false);return ResponseEntity.accepted().body(submissions.dto(s));}
  @PostMapping("/submit") ResponseEntity<Map<String,Object>> submit(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@Valid @RequestBody RunRequest request){UUID user=Api.user(p);limiter.check("submit:"+user,10);var r=rooms.byCode(roomCode);var s=submissions.enqueue(r.id,user,request.sourceCode(),request.language(),request.stdin(),true);return ResponseEntity.accepted().body(submissions.dto(s));}
  private Map<String,Object> message(ChatMessage x){var u=users.findById(x.authorId).orElse(null);var m=new LinkedHashMap<String,Object>();m.put("id",x.id);m.put("roomId",x.roomId);m.put("author",u==null?"System":u.displayName);m.put("content",x.content);m.put("type",x.type);m.put("createdAt",x.createdAt);return m;}
}
