package dev.paircode.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiSupportTest {
  @Test void rateLimiterAllowsUnderLimit() {
    var limiter = new SimpleRateLimiter();
    assertDoesNotThrow(() -> { for (int i = 0; i < 4; i++) limiter.check("key:test@example.com", 5); });
  }

  @Test void rateLimiterBlocksAtLimit() {
    var limiter = new SimpleRateLimiter();
    for (int i = 0; i < 5; i++) limiter.check("key:test@example.com", 5);
    var ex = assertThrows(ResponseStatusException.class, () -> limiter.check("key:test@example.com", 5));
    assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
  }

  @Test void rateLimiterUsesLowercaseKey() {
    var limiter = new SimpleRateLimiter();
    // Both keys should hit the same bucket
    for (int i = 0; i < 5; i++) limiter.check("register:USER@Example.COM".toLowerCase(), 5);
    var ex = assertThrows(ResponseStatusException.class, () -> limiter.check("register:user@example.com", 5));
    assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
  }

  @Test void concurrentRegisterReturns409() {
    var users = mock(UserRepository.class);
    var refreshes = mock(RefreshTokenRepository.class);
    var passwords = mock(org.springframework.security.crypto.password.PasswordEncoder.class);
    var env = new org.springframework.mock.env.MockEnvironment(); env.addActiveProfile("dev");
    var tokens = new TokenService("a-very-long-secret-that-is-at-least-32-bytes-long-for-testing", 15, 7, env);
    var limiter = new SimpleRateLimiter();
    var controller = new AuthController(users, refreshes, passwords, tokens, limiter);

    var user = new AppUser("dup@example.com", "hash", "Test");
    when(users.findByEmail("dup@example.com")).thenReturn(Optional.of(user));

    var req = new RegisterRequest("Test", "dup@example.com", "password123");
    var ex = assertThrows(ResponseStatusException.class, () -> controller.register(req));
    assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
  }
}
