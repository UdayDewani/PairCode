package dev.paircode.api;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.*;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;

record AuthPrincipal(UUID id) implements Principal { @Override public String getName(){ return id.toString(); } }

@Service
class TokenService {
  private static final String PLACEHOLDER = "change-me-in-production-change-me-in-production";
  private final Key key; private final long accessMinutes; private final long refreshDays;
  private final String secret; private final Environment env;
  TokenService(@Value("${app.jwt-secret}") String secret,@Value("${app.access-token-minutes}") long accessMinutes,@Value("${app.refresh-token-days}") long refreshDays,Environment env){
    if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalStateException("JWT_SECRET must be 32+ bytes");
    key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.accessMinutes=accessMinutes;this.refreshDays=refreshDays;this.secret=secret;this.env=env;
  }
  @PostConstruct void validateSecret(){
    boolean isDev=Arrays.asList(env.getActiveProfiles()).contains("dev");
    if(!isDev && PLACEHOLDER.equals(secret)) throw new IllegalStateException("JWT_SECRET must be changed from the default placeholder in non-dev profiles. Set the JWT_SECRET environment variable.");
  }
  String access(UUID id){return token(id,"access",Duration.ofMinutes(accessMinutes));} String refresh(UUID id){return token(id,"refresh",Duration.ofDays(refreshDays));}
  private String token(UUID id,String typ,Duration ttl){return Jwts.builder().subject(id.toString()).id(UUID.randomUUID().toString()).claim("typ",typ).issuedAt(new Date()).expiration(Date.from(Instant.now().plus(ttl))).signWith(key).compact();}
  UUID subject(String token){return UUID.fromString(parse(token).getSubject());}
  String typ(String token){Object t=parse(token).get("typ");return t instanceof String s?s:null;}
  private Claims parse(String token){return Jwts.parser().verifyWith((javax.crypto.SecretKey)key).build().parseSignedClaims(token).getPayload();}
  Instant refreshExpiry(){return Instant.now().plus(Duration.ofDays(refreshDays));}
}
@Component
class JwtFilter extends OncePerRequestFilter {
  private final TokenService tokens; JwtFilter(TokenService tokens){this.tokens=tokens;}
  @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws IOException,jakarta.servlet.ServletException{
    String header=req.getHeader("Authorization");
    if(header!=null&&header.startsWith("Bearer ")) try { String raw=header.substring(7); if("access".equals(tokens.typ(raw))){UUID id=tokens.subject(raw); var a=new UsernamePasswordAuthenticationToken(new AuthPrincipal(id),null,List.of()); SecurityContextHolder.getContext().setAuthentication(a);} } catch (JwtException|IllegalArgumentException ignored) {}
    chain.doFilter(req,res);
  }
}
@Configuration @EnableWebSecurity
class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
  @Bean(name="corsConfigurationSource") CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-origin}") String origin){
    var c=new CorsConfiguration();c.setAllowedOrigins(List.of(origin));c.setAllowedMethods(List.of("GET","POST","PATCH","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));c.setExposedHeaders(List.of("Authorization"));c.setAllowCredentials(true);c.setMaxAge(3600L);var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",c);return source;
  }
  @Bean SecurityFilterChain security(HttpSecurity http,JwtFilter jwt,CorsConfigurationSource corsConfigurationSource)throws Exception{return http.csrf(c->c.disable()).cors(c->c.configurationSource(corsConfigurationSource)).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(e->e.authenticationEntryPoint(new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED))).authorizeHttpRequests(a->a.requestMatchers("/api/v1/auth/**","/api/v1/ws-ticket","/swagger-ui/**","/v3/api-docs/**","/actuator/health","/ws/**").permitAll().requestMatchers(HttpMethod.OPTIONS,"/**").permitAll().anyRequest().authenticated()).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();}
}
