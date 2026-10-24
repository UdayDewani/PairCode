package dev.paircode.api;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {
  private static final String VALID_SECRET = "a-very-long-secret-that-is-at-least-32-bytes-long-for-testing";

  @Test void accessTokenHasTypAccess() {
    var env = new MockEnvironment(); env.addActiveProfile("dev");
    var ts = new TokenService(VALID_SECRET, 15, 7, env);
    var token = ts.access(java.util.UUID.randomUUID());
    assertEquals("access", ts.typ(token));
  }

  @Test void refreshTokenHasTypRefresh() {
    var env = new MockEnvironment(); env.addActiveProfile("dev");
    var ts = new TokenService(VALID_SECRET, 15, 7, env);
    var token = ts.refresh(java.util.UUID.randomUUID());
    assertEquals("refresh", ts.typ(token));
  }

  @Test void accessTokenRoundtrips() {
    var env = new MockEnvironment(); env.addActiveProfile("dev");
    var ts = new TokenService(VALID_SECRET, 15, 7, env);
    var id = java.util.UUID.randomUUID();
    var token = ts.access(id);
    assertEquals(id, ts.subject(token));
  }

  @Test void placeholderSecretFailsOnNonDevProfile() {
    var env = new MockEnvironment(); // no active profiles
    var ts = new TokenService("change-me-in-production-change-me-in-production", 15, 7, env);
    assertThrows(IllegalStateException.class, ts::validateSecret);
  }

  @Test void placeholderSecretAllowedOnDevProfile() {
    var env = new MockEnvironment(); env.addActiveProfile("dev");
    var ts = new TokenService("change-me-in-production-change-me-in-production", 15, 7, env);
    assertDoesNotThrow(ts::validateSecret);
  }
}
