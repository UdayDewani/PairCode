package dev.paircode.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

record ApiError(Instant timestamp,int status,String code,String message,String path) {}
@RestControllerAdvice
class ApiExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ApiError> response(ResponseStatusException e,HttpServletRequest r){return ResponseEntity.status(e.getStatusCode()).body(new ApiError(Instant.now(),e.getStatusCode().value(),"REQUEST_FAILED",e.getReason(),r.getRequestURI()));}
  @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unhandled(Exception e,HttpServletRequest r){return ResponseEntity.status(500).body(new ApiError(Instant.now(),500,"INTERNAL_ERROR","Unexpected server error",r.getRequestURI()));}
}
@org.springframework.stereotype.Component
class SimpleRateLimiter {
  private final Map<String,Deque<Long>> requests=new ConcurrentHashMap<>();
  void check(String key,int limit){long now=System.currentTimeMillis();var q=requests.computeIfAbsent(key,k->new ConcurrentLinkedDeque<>());while(q.peekFirst()!=null&&q.peekFirst()<now-60_000)q.pollFirst();if(q.size()>=limit)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Rate limit exceeded");q.addLast(now);}
}
final class Api {
  private Api(){} static UUID user(AuthPrincipal p){if(p==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authentication required");return p.id();}
  static Map<String,Object> user(AppUser u){return Map.of("id",u.id,"name",u.displayName,"email",u.email,"preferredLanguage",u.preferredLanguage);}
  static Map<String,Object> problem(Problem p){var m=new LinkedHashMap<String,Object>();m.put("id",p.id);m.put("number",p.number);m.put("title",p.title);m.put("slug",p.slug);m.put("difficulty",p.difficulty);m.put("topic",p.topic);m.put("description",p.description);m.put("constraints",Optional.ofNullable(p.constraintsText).orElse(""));m.put("examples",p.examples);m.put("starterCode",p.starterCode);m.put("acceptanceRate",p.acceptanceRate);return m;}
}
