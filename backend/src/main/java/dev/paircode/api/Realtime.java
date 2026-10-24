package dev.paircode.api;

import com.fasterxml.jackson.databind.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.*;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

@Configuration @EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {
  private final RoomSocketHandler handler; private final SocketAuth auth;
  WebSocketConfig(RoomSocketHandler handler,SocketAuth auth){this.handler=handler;this.auth=auth;}
  @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry r){r.addHandler(handler,"/ws").addInterceptors(auth).setAllowedOriginPatterns("*");}
}
@Component
class SocketAuth implements HandshakeInterceptor {
  private final TokenService tokens; SocketAuth(TokenService tokens){this.tokens=tokens;}
  public boolean beforeHandshake(ServerHttpRequest request,ServerHttpResponse response,WebSocketHandler handler,Map<String,Object> attributes){try{String query=Optional.ofNullable(request.getURI().getQuery()).orElse("");String token=Arrays.stream(query.split("&")).filter(p->p.startsWith("token=")).map(p->p.substring(6)).findFirst().orElseThrow();attributes.put("userId",tokens.subject(java.net.URLDecoder.decode(token,java.nio.charset.StandardCharsets.UTF_8)));return true;}catch(Exception e){return false;}}
  public void afterHandshake(ServerHttpRequest request,ServerHttpResponse response,WebSocketHandler handler,Exception ex){}
}
@Service
class RealtimeService {
  private final Map<String,Set<WebSocketSession>> roomSessions=new ConcurrentHashMap<>(); private final Map<String,String> sessionRooms=new ConcurrentHashMap<>(); private final ObjectMapper json;
  RealtimeService(ObjectMapper json){this.json=json;}
  void join(String room,WebSocketSession session){roomSessions.computeIfAbsent(room,k->ConcurrentHashMap.newKeySet()).add(session);sessionRooms.put(session.getId(),room);}
  void leave(WebSocketSession session){String room=sessionRooms.remove(session.getId());if(room!=null){var sessions=roomSessions.get(room);if(sessions!=null)sessions.remove(session);}}
  void event(String room,String type,Object data){try{var payload=json.writeValueAsString(Map.of("type",type,"data",data));for(var session:roomSessions.getOrDefault(room,Set.of()))send(session,new TextMessage(payload));}catch(Exception ignored){}}
  void binary(String room,WebSocketSession origin,ByteBuffer data){byte[] b=new byte[data.remaining()];data.get(b);for(var session:roomSessions.getOrDefault(room,Set.of()))if(!session.getId().equals(origin.getId()))send(session,new BinaryMessage(b));}
  String roomOf(WebSocketSession session){return sessionRooms.get(session.getId());} private void send(WebSocketSession s,WebSocketMessage<?> message){try{if(s.isOpen())s.sendMessage(message);}catch(IOException ignored){}}
}
@Component
class RoomSocketHandler extends AbstractWebSocketHandler {
  private final ObjectMapper json; private final RoomService rooms; private final RealtimeService realtime; private final RoomRepository roomRepository;
  RoomSocketHandler(ObjectMapper json,RoomService rooms,RealtimeService realtime,RoomRepository roomRepository){this.json=json;this.rooms=rooms;this.realtime=realtime;this.roomRepository=roomRepository;}
  @Override protected void handleTextMessage(WebSocketSession session,TextMessage message)throws Exception {var node=json.readTree(message.getPayload());String type=node.path("type").asText();UUID user=(UUID)session.getAttributes().get("userId");if("join".equals(type)){var room=rooms.byCode(node.path("roomId").asText());rooms.member(room.id,user);realtime.join(room.roomCode,session);realtime.event(room.roomCode,"participant.list",rooms.participants(room.id));return;}String roomCode=realtime.roomOf(session);if(roomCode==null)throw new IllegalStateException("Join a room first");var room=rooms.byCode(roomCode);if("awareness".equals(type)){rooms.member(room.id,user);realtime.event(roomCode,"awareness",node.path("payload"));}else if("doc.update".equals(type)){rooms.canEdit(room.id,user);String update=node.path("payload").asText();room.documentState=Base64.getDecoder().decode(update);roomRepository.save(room);realtime.event(roomCode,"doc.update",Map.of("payload",update));}else if("leave".equals(type)){realtime.leave(session);realtime.event(roomCode,"participant.list",rooms.participants(room.id));}else realtime.event(roomCode,"error",Map.of("message","Unknown message type"));}
  @Override protected void handleBinaryMessage(WebSocketSession session,BinaryMessage message){String code=realtime.roomOf(session);if(code==null)return;UUID user=(UUID)session.getAttributes().get("userId");var room=rooms.byCode(code);rooms.canEdit(room.id,user);byte[] update=new byte[message.getPayload().remaining()];message.getPayload().get(update);room.documentState=update;roomRepository.save(room);realtime.binary(code,session,ByteBuffer.wrap(update));}
  @Override public void afterConnectionClosed(WebSocketSession session,CloseStatus status){String room=realtime.roomOf(session);realtime.leave(session);if(room!=null){try{var r=rooms.byCode(room);realtime.event(room,"participant.list",rooms.participants(r.id));}catch(Exception ignored){}}}
  @Override public void handleTransportError(WebSocketSession session,Throwable exception)throws Exception {if(session.isOpen())session.close(CloseStatus.SERVER_ERROR);}
}
