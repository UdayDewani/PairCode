package dev.paircode.api;

import com.fasterxml.jackson.databind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Configuration @EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {
  private final RoomSocketHandler handler; private final SocketAuth auth; private final String origin;
  WebSocketConfig(RoomSocketHandler handler,SocketAuth auth,@Value("${app.frontend-origin}") String origin){this.handler=handler;this.auth=auth;this.origin=origin;}
  @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry r){r.addHandler(handler,"/ws").addInterceptors(auth).setAllowedOrigins(origin);}
  @Bean ServletServerContainerFactoryBean createWebSocketContainer(){var b=new ServletServerContainerFactoryBean();b.setMaxTextMessageBufferSize(1024*1024);b.setMaxBinaryMessageBufferSize(1024*1024);b.setMaxSessionIdleTimeout(60_000L);return b;}
}

/** Short-lived single-use tickets issued by REST, consumed at WS handshake. */
@Component
class SocketAuth implements HandshakeInterceptor {
  private static final Logger log = LoggerFactory.getLogger(SocketAuth.class);
  // ticket -> {userId, expiresAt}
  private final ConcurrentHashMap<String,Object[]> tickets=new ConcurrentHashMap<>();
  private final TokenService tokens; SocketAuth(TokenService tokens){this.tokens=tokens;}

  /** Issue a 30-second single-use ticket (called from WsTicketController). */
  String issue(UUID userId){String t=UUID.randomUUID().toString();tickets.put(t,new Object[]{userId,Instant.now().plusSeconds(30)});return t;}

  @Scheduled(fixedDelay=30_000) void purge(){Instant now=Instant.now();tickets.entrySet().removeIf(e->((Instant)e.getValue()[1]).isBefore(now));}

  @Override public boolean beforeHandshake(ServerHttpRequest request,ServerHttpResponse response,WebSocketHandler handler,Map<String,Object> attributes){
    try{
      String query=Optional.ofNullable(request.getURI().getQuery()).orElse("");
      // Support ticket= (preferred) and legacy token= (for backward compat)
      String ticket=Arrays.stream(query.split("&")).filter(p->p.startsWith("ticket=")).map(p->p.substring(7)).findFirst().orElse(null);
      if(ticket!=null){
        var entry=tickets.remove(ticket);
        if(entry==null)return false;
        if(((Instant)entry[1]).isBefore(Instant.now()))return false;
        attributes.put("userId",entry[0]);return true;
      }
      // fallback: token= with typ=access check
      String token=Arrays.stream(query.split("&")).filter(p->p.startsWith("token=")).map(p->p.substring(6)).findFirst().orElseThrow();
      String decoded=java.net.URLDecoder.decode(token,java.nio.charset.StandardCharsets.UTF_8);
      if(!"access".equals(tokens.typ(decoded)))return false;
      attributes.put("userId",tokens.subject(decoded));return true;
    }catch(Exception e){log.warn("WS handshake rejected: {}",e.getMessage());return false;}
  }
  @Override public void afterHandshake(ServerHttpRequest request,ServerHttpResponse response,WebSocketHandler handler,Exception ex){}
}

@RestController @RequestMapping("/api/v1")
class WsTicketController {
  private final SocketAuth auth;
  WsTicketController(SocketAuth auth){this.auth=auth;}
  @PostMapping("/ws-ticket") Map<String,Object> ticket(@AuthenticationPrincipal AuthPrincipal p){
    UUID userId=Api.user(p);String t=auth.issue(userId);
    return Map.of("ticket",t,"expiresAt",Instant.now().plusSeconds(30));
  }
}

@Service
class RealtimeService {
  private static final Logger log = LoggerFactory.getLogger(RealtimeService.class);
  // room code -> set of wrapped sessions
  private final Map<String,Set<WebSocketSession>> roomSessions=new ConcurrentHashMap<>();
  // session id -> room code
  private final Map<String,String> sessionRooms=new ConcurrentHashMap<>();
  // session id -> user id
  private final Map<String,UUID> sessionUsers=new ConcurrentHashMap<>();
  private final ObjectMapper json;
  RealtimeService(ObjectMapper json){this.json=json;}

  WebSocketSession wrap(WebSocketSession session){return new ConcurrentWebSocketSessionDecorator(session,1000,1024*1024);}
  void join(String room,WebSocketSession session,UUID userId){
    // Leave any previous room
    String prev=sessionRooms.get(session.getId());if(prev!=null&&!prev.equals(room)){var ps=roomSessions.get(prev);if(ps!=null)ps.remove(session);}
    roomSessions.computeIfAbsent(room,k->ConcurrentHashMap.newKeySet()).add(session);
    sessionRooms.put(session.getId(),room);sessionUsers.put(session.getId(),userId);
  }
  void leave(WebSocketSession session){
    String room=sessionRooms.remove(session.getId());sessionUsers.remove(session.getId());
    if(room!=null){var sessions=roomSessions.get(room);if(sessions!=null){sessions.remove(session);if(sessions.isEmpty())roomSessions.remove(room);}}
  }
  /** Close all WebSocket sessions belonging to this userId. */
  void evict(UUID userId){
    List<WebSocketSession> toClose=new ArrayList<>();
    for(var e:sessionUsers.entrySet()){if(userId.equals(e.getValue())){var s=findSession(e.getKey());if(s!=null)toClose.add(s);}}
    for(var s:toClose){try{s.close(CloseStatus.POLICY_VIOLATION);}catch(IOException ignored){}leave(s);}
  }
  private WebSocketSession findSession(String id){
    for(var sessions:roomSessions.values())for(var s:sessions)if(s.getId().equals(id))return s;return null;
  }
  boolean isOnline(UUID userId){return sessionUsers.containsValue(userId);}
  Set<WebSocketSession> sessionsFor(String room){return roomSessions.getOrDefault(room,Set.of());}
  void broadcastTextExcept(String room,String excludeSessionId,String payload){
    for(var s:roomSessions.getOrDefault(room,Set.of())){
      if(s.getId().equals(excludeSessionId))continue;
      try{if(s.isOpen())s.sendMessage(new TextMessage(payload));}
      catch(Exception ex){log.warn("Failed to broadcast to session {}: {}",s.getId(),ex.getMessage());}
    }
  }
  void event(String room,String type,Object data){
    try{var payload=json.writeValueAsString(Map.of("type",type,"data",data));
      for(var session:roomSessions.getOrDefault(room,Set.of())){
        try{if(session.isOpen())session.sendMessage(new TextMessage(payload));}
        catch(Exception ex){log.warn("Failed to send event to session {}: {}",session.getId(),ex.getMessage());}
      }
    }catch(Exception ignored){}
  }
  void eventToSession(WebSocketSession session,String type,Object data){
    try{var payload=json.writeValueAsString(Map.of("type",type,"data",data));if(session.isOpen())session.sendMessage(new TextMessage(payload));}
    catch(Exception ex){log.warn("Failed to send event to session {}: {}",session.getId(),ex.getMessage());}
  }
  void binary(String room,WebSocketSession origin,byte[] data){
    for(var session:roomSessions.getOrDefault(room,Set.of())){
      if(session.getId().equals(origin.getId()))continue;
      try{if(session.isOpen())session.sendMessage(new BinaryMessage(data));}
      catch(Exception ex){log.warn("Failed to send binary to session {}: {}",session.getId(),ex.getMessage());}
    }
  }
  String roomOf(WebSocketSession session){return sessionRooms.get(session.getId());}
}

@Component
class RoomSocketHandler extends AbstractWebSocketHandler {
  private static final Logger log = LoggerFactory.getLogger(RoomSocketHandler.class);
  private final ObjectMapper json; private final RoomService rooms; private final RealtimeService realtime; private final RoomRepository roomRepository;
  // debounce: room id -> dirty state pending flush
  private final ConcurrentHashMap<UUID,byte[]> dirtyDocs=new ConcurrentHashMap<>();
  private final java.util.concurrent.ScheduledExecutorService flusher=java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"doc-flusher");t.setDaemon(true);return t;});
  RoomSocketHandler(ObjectMapper json,RoomService rooms,RealtimeService realtime,RoomRepository roomRepository){
    this.json=json;this.rooms=rooms;this.realtime=realtime;this.roomRepository=roomRepository;
    flusher.scheduleAtFixedRate(this::flushDirtyDocs,1,1,java.util.concurrent.TimeUnit.SECONDS);
  }
  private void flushDirtyDocs(){
    dirtyDocs.forEach((id,state)->{ try{roomRepository.updateDocumentState(id,state);}catch(Exception ex){log.error("Failed to flush doc state for room {}",id,ex);} });
    dirtyDocs.clear();
  }
  private void markDirty(UUID roomId,byte[] state){dirtyDocs.put(roomId,state);}

  @Override public void afterConnectionEstablished(WebSocketSession session)throws Exception{
    // Wrap the session for concurrent safety
  }

  @Override protected void handleTextMessage(WebSocketSession session,TextMessage message)throws Exception {
    UUID user=(UUID)session.getAttributes().get("userId");
    try{
      var node=json.readTree(message.getPayload());String type=node.path("type").asText();
      if("join".equals(type)){
        String code=node.path("roomId").asText().toUpperCase();
        var room=rooms.byCode(code);
        if(room.status==RoomStatus.ENDED){realtime.eventToSession(session,"error",Map.of("message","Session has ended"));return;}
        // Check participant: join only if leftAt is null or was previously a member (not removed)
        rooms.joinWs(room.id,user);
        realtime.join(room.roomCode,session,user);
        // Send current document state to the joining session
        if(room.documentState!=null&&room.documentState.length>0){
          String state=Base64.getEncoder().encodeToString(room.documentState);
          realtime.eventToSession(session,"doc.state",Map.of("payload",state));
        }
        realtime.event(room.roomCode,"participant.list",rooms.participants(room.id));
        return;
      }
      String roomCode=realtime.roomOf(session);
      if(roomCode==null){realtime.eventToSession(session,"error",Map.of("message","Join a room first"));return;}
      var room=rooms.byCode(roomCode);
      if(room.status==RoomStatus.ENDED&&!"leave".equals(type)){realtime.eventToSession(session,"error",Map.of("message","Session has ended"));return;}
      switch(type){
        case "awareness"->{
          rooms.member(room.id,user);
          // broadcast to others only (no echo)
          try{var payload=json.writeValueAsString(Map.of("type","awareness","data",node.path("payload")));
            realtime.broadcastTextExcept(room.roomCode,session.getId(),payload);
          }catch(Exception ex){log.warn("awareness broadcast error",ex);}
        }
        case "doc.update"->{
          rooms.canEdit(room.id,user);
          String update=node.path("payload").asText();
          byte[] state=Base64.getDecoder().decode(update);
          markDirty(room.id,state);
          // broadcast to others only (no echo)
          try{var payload=json.writeValueAsString(Map.of("type","doc.update","data",Map.of("payload",update)));
            realtime.broadcastTextExcept(room.roomCode,session.getId(),payload);
          }catch(Exception ex){log.warn("doc.update broadcast error",ex);}
        }
        case "leave"->{
          flushRoomDoc(room.id);
          realtime.leave(session);
          realtime.event(roomCode,"participant.list",rooms.participants(room.id));
        }
        default->realtime.eventToSession(session,"error",Map.of("message","Unknown message type: "+type));
      }
    }catch(ResponseStatusException ex){
      realtime.eventToSession(session,"error",Map.of("message",ex.getReason()!=null?ex.getReason():"Permission denied"));
    }catch(Exception ex){
      log.error("Error handling WS message from session {}",session.getId(),ex);
      realtime.eventToSession(session,"error",Map.of("message","Internal error"));
    }
  }

  @Override protected void handleBinaryMessage(WebSocketSession session,BinaryMessage message){
    String code=realtime.roomOf(session);if(code==null)return;
    UUID user=(UUID)session.getAttributes().get("userId");
    try{
      var room=rooms.byCode(code);
      if(room.status==RoomStatus.ENDED)return;
      rooms.canEdit(room.id,user);
      byte[] update=new byte[message.getPayload().remaining()];message.getPayload().get(update);
      markDirty(room.id,update);
      realtime.binary(code,session,update);
    }catch(ResponseStatusException ex){
      realtime.eventToSession(session,"error",Map.of("message",ex.getReason()!=null?ex.getReason():"Permission denied"));
    }catch(Exception ex){log.error("Error handling binary WS message",ex);}
  }

  @Override public void afterConnectionClosed(WebSocketSession session,CloseStatus status){
    String room=realtime.roomOf(session);
    if(room!=null){try{var r=rooms.byCode(room);flushRoomDoc(r.id);}catch(Exception ignored){}}
    realtime.leave(session);
    if(room!=null){try{var r=rooms.byCode(room);realtime.event(room,"participant.list",rooms.participants(r.id));}catch(Exception ignored){}}
  }

  @Override public void handleTransportError(WebSocketSession session,Throwable exception)throws Exception {
    log.warn("WS transport error on session {}: {}",session.getId(),exception.getMessage());
    if(session.isOpen())session.close(CloseStatus.SERVER_ERROR);
  }

  private void flushRoomDoc(UUID roomId){byte[] state=dirtyDocs.remove(roomId);if(state!=null)try{roomRepository.updateDocumentState(roomId,state);}catch(Exception ex){log.error("Failed to flush doc on leave for room {}",roomId,ex);}}
}
