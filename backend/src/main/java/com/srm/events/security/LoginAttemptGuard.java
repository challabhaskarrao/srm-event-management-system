package com.srm.events.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptGuard {
    private static final int MAX_TRACKED_KEYS = 10_000;
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final int maximumAttempts;
    private final Duration window;
    private final Duration lockDuration;
    private final Clock clock;

    @Autowired
    public LoginAttemptGuard(
            @Value("${app.security.login.max-attempts:5}") int maximumAttempts,
            @Value("${app.security.login.window-seconds:900}") long windowSeconds,
            @Value("${app.security.login.lock-seconds:900}") long lockSeconds) {
        this(maximumAttempts, Duration.ofSeconds(windowSeconds), Duration.ofSeconds(lockSeconds), Clock.systemUTC());
    }

    LoginAttemptGuard(int maximumAttempts, Duration window, Duration lockDuration, Clock clock) {
        this.maximumAttempts = maximumAttempts;
        this.window = window;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    public boolean isBlocked(String accountKey, String clientAddress) {
        Instant now = clock.instant();
        return blocked("account:" + accountKey, now) || blocked("client:" + safeClient(clientAddress), now);
    }

    public void recordFailure(String accountKey, String clientAddress) {
        Instant now = clock.instant();
        record("account:" + accountKey, now);
        record("client:" + safeClient(clientAddress), now);
        if (attempts.size() > MAX_TRACKED_KEYS) attempts.entrySet().removeIf(entry -> entry.getValue().expired(now, window, lockDuration));
    }

    public void recordSuccess(String accountKey, String clientAddress) {
        attempts.remove("account:" + accountKey);
        attempts.remove("client:" + safeClient(clientAddress));
    }

    private boolean blocked(String key, Instant now) {
        AttemptState state = attempts.get(key);
        if (state == null) return false;
        if (state.expired(now, window, lockDuration)) {
            attempts.remove(key, state);
            return false;
        }
        return state.lockedUntil != null && now.isBefore(state.lockedUntil);
    }

    private void record(String key, Instant now) {
        if (!attempts.containsKey(key) && attempts.size() >= MAX_TRACKED_KEYS) return;
        attempts.compute(key, (ignored, current) -> {
            AttemptState state = current == null || current.windowStarted.plus(window).isBefore(now)
                    ? new AttemptState(0, now, null) : current;
            int failures = state.failures + 1;
            Instant lockedUntil = failures >= maximumAttempts ? now.plus(lockDuration) : state.lockedUntil;
            return new AttemptState(failures, state.windowStarted, lockedUntil);
        });
    }

    private String safeClient(String clientAddress) {
        return clientAddress == null || clientAddress.isBlank() ? "unknown" : clientAddress;
    }

    private record AttemptState(int failures, Instant windowStarted, Instant lockedUntil) {
        boolean expired(Instant now, Duration window, Duration lockDuration) {
            Instant lastRelevant = lockedUntil == null ? windowStarted.plus(window) : lockedUntil.plus(lockDuration);
            return now.isAfter(lastRelevant);
        }
    }
}
