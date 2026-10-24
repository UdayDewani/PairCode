package dev.paircode.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

record RegisterRequest(@NotBlank @Size(max=100) String name,@NotBlank @Email String email,@NotBlank @Size(min=8,max=128) String password) {}
record LoginRequest(@NotBlank @Email String email,@NotBlank String password) {}
record RefreshRequest(@NotBlank String refreshToken) {}
record ProfileUpdateRequest(@NotBlank @Size(max=100) String name,@Pattern(regexp="JAVA|PYTHON|CPP") String preferredLanguage) {}
record PasswordChangeRequest(@NotBlank String currentPassword,@NotBlank @Size(min=8,max=128) String newPassword) {}

@RestController @RequestMapping("/api/v1")
class AuthController {
  private final UserRepository users; private final RefreshTokenRepository refreshes; private final PasswordEncoder passwords; private final TokenService tokens; private final SimpleRateLimiter limiter;
  AuthController(UserRepository users,RefreshTokenRepository refreshes,PasswordEncoder passwords,TokenService tokens,SimpleRateLimiter limiter){this.users=users;this.refreshes=refreshes;this.passwords=passwords;this.tokens=tokens;this.limiter=limiter;}
  @PostMapping("/auth/register") @Transactional ResponseEntity<Map<String,Object>> register(@Valid @RequestBody RegisterRequest r){
    limiter.check("register:"+r.email(),5); if(users.findByEmail(r.email().toLowerCase()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
    var user=users.save(new AppUser(r.email(),passwords.encode(r.password()),r.name())); return ResponseEntity.status(HttpStatus.CREATED).body(auth(user));
  }
  @PostMapping("/auth/login") ResponseEntity<Map<String,Object>> login(@Valid @RequestBody LoginRequest r){
    limiter.check("login:"+r.email(),10);var user=users.findByEmail(r.email().toLowerCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));if(!passwords.matches(r.password(),user.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");return ResponseEntity.ok(auth(user));
  }
  @PostMapping("/auth/refresh") ResponseEntity<Map<String,Object>> refresh(@Valid @RequestBody RefreshRequest r){
    UUID userId;try{userId=tokens.subject(r.refreshToken());}catch(Exception e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid refresh token");}var saved=refreshes.findByTokenHash(hash(r.refreshToken())).filter(x->x.revokedAt==null&&x.expiresAt.isAfter(Instant.now())).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Refresh token expired or revoked"));if(!saved.userId.equals(userId))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid refresh token");return ResponseEntity.ok(auth(users.findById(userId).orElseThrow()));}
  @PostMapping("/auth/logout") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional void logout(@Valid @RequestBody RefreshRequest r){refreshes.findByTokenHash(hash(r.refreshToken())).ifPresent(t->{t.revokedAt=Instant.now();refreshes.save(t);});}
  @GetMapping("/profile") Map<String,Object> profile(@AuthenticationPrincipal AuthPrincipal p){return Api.user(users.findById(Api.user(p)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found")));}
  @PatchMapping("/profile") Map<String,Object> update(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody ProfileUpdateRequest r){var u=users.findById(Api.user(p)).orElseThrow();u.displayName=r.name();if(r.preferredLanguage()!=null)u.preferredLanguage=r.preferredLanguage();u.updatedAt=Instant.now();return Api.user(users.save(u));}
  @PostMapping("/profile/password") @ResponseStatus(HttpStatus.NO_CONTENT) void password(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody PasswordChangeRequest r){var u=users.findById(Api.user(p)).orElseThrow();if(!passwords.matches(r.currentPassword(),u.passwordHash))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Current password is incorrect");u.passwordHash=passwords.encode(r.newPassword());u.updatedAt=Instant.now();users.save(u);}
  private Map<String,Object> auth(AppUser u){String refresh=tokens.refresh(u.id);refreshes.save(new RefreshToken(u.id,hash(refresh),tokens.refreshExpiry()));return Map.of("accessToken",tokens.access(u.id),"refreshToken",refresh,"user",Api.user(u));}
  private String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
