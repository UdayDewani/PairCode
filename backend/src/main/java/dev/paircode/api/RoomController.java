package dev.paircode.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

record CreateRoomRequest(@NotBlank @Size(max=200) String name,UUID problemId,@Pattern(regexp="JAVA|PYTHON|CPP") String language,Boolean candidateCanEdit) {}
record PermissionRequest(Boolean candidateCanEdit,Boolean editorLocked) {}
record ProblemChangeRequest(@NotNull UUID problemId) {}
record LanguageChangeRequest(@NotBlank @Pattern(regexp="JAVA|PYTHON|CPP") String language) {}
record EndRoomRequest(String sourceCode) {}
record SnapshotRequest(@NotBlank String sourceCode) {}
@RestController @RequestMapping("/api/v1/rooms")
class RoomController {
  private final RoomService service; private final RealtimeService realtime; RoomController(RoomService service,RealtimeService realtime){this.service=service;this.realtime=realtime;}
  @PostMapping ResponseEntity<Map<String,Object>> create(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody CreateRoomRequest r){var room=service.create(Api.user(p),r.name(),r.problemId(),r.language(),!Boolean.FALSE.equals(r.candidateCanEdit()));return ResponseEntity.status(HttpStatus.CREATED).body(service.dto(room));}
  @GetMapping("/{roomCode}") Map<String,Object> get(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode){var r=service.byCode(roomCode);service.member(r.id,Api.user(p));return service.dto(r);}
  @PostMapping("/{roomCode}/join") Map<String,Object> join(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode){return service.dto(service.join(Api.user(p),roomCode));}
  @GetMapping("/{roomCode}/participants") List<Map<String,Object>> participants(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode){var r=service.byCode(roomCode);service.member(r.id,Api.user(p));return service.participants(r.id);}
  @PatchMapping("/{roomCode}/permissions") Map<String,Object> permissions(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@RequestBody PermissionRequest r){var room=service.byCode(roomCode);var changed=service.permission(room.id,Api.user(p),r.candidateCanEdit(),r.editorLocked());var result=service.dto(changed);realtime.event(changed.roomCode,"permission.changed",result);return result;}
  @PatchMapping("/{roomCode}/problem") Map<String,Object> problem(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@Valid @RequestBody ProblemChangeRequest r){var room=service.byCode(roomCode);var changed=service.changeProblem(room.id,Api.user(p),r.problemId());var result=service.dto(changed);realtime.event(changed.roomCode,"problem.changed",result);return result;}
  @PatchMapping("/{roomCode}/language")
  Map<String,Object> language(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@Valid @RequestBody LanguageChangeRequest r){
    var room=service.byCode(roomCode);
    var changed=service.changeLanguage(room.id,Api.user(p),r.language());
    var result=service.dto(changed);
    realtime.event(changed.roomCode,"language.changed",result);
    return result;
  }
  @DeleteMapping("/{roomCode}/participants/{userId}") @ResponseStatus(HttpStatus.NO_CONTENT) void remove(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@PathVariable UUID userId){var room=service.byCode(roomCode);service.removeParticipant(room.id,Api.user(p),userId);realtime.event(room.roomCode,"participant.list",service.participants(room.id));}
  @PostMapping("/{roomCode}/snapshots") @ResponseStatus(HttpStatus.CREATED) void snapshot(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@Valid @RequestBody SnapshotRequest r){var room=service.byCode(roomCode);service.canEdit(room.id,Api.user(p));service.snapshot(room.id,Api.user(p),r.sourceCode(),"MANUAL");}
  @PostMapping("/{roomCode}/end") @ResponseStatus(HttpStatus.NO_CONTENT) void end(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String roomCode,@RequestBody(required=false) EndRoomRequest r){var room=service.byCode(roomCode);service.end(room.id,Api.user(p),r==null?null:r.sourceCode());realtime.event(room.roomCode,"session.ended",Map.of("roomId",room.roomCode));}
}
