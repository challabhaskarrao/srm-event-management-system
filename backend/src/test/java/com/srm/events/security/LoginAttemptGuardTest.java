package com.srm.events.security;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptGuardTest {
    @Test
    void temporarilyBlocksAfterBoundedFailuresAndSuccessClearsState() {
        LoginAttemptGuard guard = new LoginAttemptGuard(3, Duration.ofMinutes(15), Duration.ofMinutes(15),
                Clock.fixed(Instant.parse("2026-09-18T10:00:00Z"), ZoneOffset.UTC));
        assertFalse(guard.isBlocked("user@example.com", "127.0.0.1"));
        guard.recordFailure("user@example.com", "127.0.0.1");
        guard.recordFailure("user@example.com", "127.0.0.1");
        guard.recordFailure("user@example.com", "127.0.0.1");
        assertTrue(guard.isBlocked("user@example.com", "127.0.0.1"));
        guard.recordSuccess("user@example.com", "127.0.0.1");
        assertFalse(guard.isBlocked("user@example.com", "127.0.0.1"));
    }
}
