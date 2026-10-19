package com.skillmirror.security;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Simple in-memory rate limiter protecting the login endpoint against brute force.
 * Limits: 8 failed attempts per e-mail within 10 minutes.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 8;
    private static final long WINDOW_SECONDS = 600;

    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        Instant now = Instant.now();
        Deque<Instant> deque = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst().isBefore(now.minusSeconds(WINDOW_SECONDS))) {
                deque.pollFirst();
            }
            return deque.size() < MAX_ATTEMPTS;
        }
    }

    public void recordFailure(String key) {
        Deque<Instant> deque = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addLast(Instant.now());
        }
    }

    public void reset(String key) {
        attempts.remove(key);
    }
}
