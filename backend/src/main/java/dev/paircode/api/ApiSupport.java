package dev.paircode.api;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

record ApiError(Instant timestamp,int status,String code,String message,String path) {}
@RestControllerAdvice
class ApiExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
  @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ApiError> response(ResponseStatusException e,HttpServletRequest r){return ResponseEntity.status(e.getStatusCode()).body(new ApiError(Instant.now(),e.getStatusCode().value(),"REQUEST_FAILED",e.getReason(),r.getRequestURI()));}
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException e,HttpServletRequest r){
    String msg=e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).collect(Collectors.joining("; "));
    return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"VALIDATION_FAILED",msg,r.getRequestURI()));
  }
  @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e,HttpServletRequest r){return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"BAD_REQUEST","Malformed or missing request body",r.getRequestURI()));}
  @ExceptionHandler(NoSuchElementException.class) ResponseEntity<ApiError> notFound(NoSuchElementException e,HttpServletRequest r){return ResponseEntity.status(404).body(new ApiError(Instant.now(),404,"NOT_FOUND",e.getMessage()!=null?e.getMessage():"Resource not found",r.getRequestURI()));}
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ApiError> conflict(DataIntegrityViolationException e,HttpServletRequest r){return ResponseEntity.status(409).body(new ApiError(Instant.now(),409,"CONFLICT","A conflicting resource already exists",r.getRequestURI()));}
  @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unhandled(Exception e,HttpServletRequest r){log.error("Unhandled exception at {}",r.getRequestURI(),e);return ResponseEntity.status(500).body(new ApiError(Instant.now(),500,"INTERNAL_ERROR","Unexpected server error",r.getRequestURI()));}
}
@org.springframework.stereotype.Component
class SimpleRateLimiter {
  private final ConcurrentHashMap<String,Deque<Long>> requests=new ConcurrentHashMap<>();
  void check(String key,int limit){
    long now=System.currentTimeMillis();
    var q=requests.computeIfAbsent(key,k->new ArrayDeque<>());
    synchronized(q){
      while(!q.isEmpty()&&q.peekFirst()<now-60_000)q.pollFirst();
      if(q.size()>=limit)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Rate limit exceeded");
      q.addLast(now);
    }
    // Evict empty buckets periodically (avoid unbounded growth)
    if(Math.random()<0.01)requests.entrySet().removeIf(e->{synchronized(e.getValue()){return e.getValue().isEmpty();}});
  }
}
record WsTicket(String ticket,Instant expiresAt){}
final class Api {
  private Api(){} static UUID user(AuthPrincipal p){if(p==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authentication required");return p.id();}
  static Map<String,Object> user(AppUser u){return Map.of("id",u.id,"name",u.displayName,"email",u.email,"preferredLanguage",u.preferredLanguage);}
  static Map<String,Object> problem(Problem p){var m=new LinkedHashMap<String,Object>();m.put("id",p.id);m.put("number",p.number);m.put("title",p.title);m.put("slug",p.slug);m.put("difficulty",p.difficulty);m.put("topic",p.topic);m.put("description",p.description);m.put("constraints",Optional.ofNullable(p.constraintsText).orElse(""));m.put("examples",p.examples);m.put("starterCode",p.starterCode);m.put("acceptanceRate",p.acceptanceRate);return m;}
}
