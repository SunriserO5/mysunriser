package com.mysunriser.backend.service;

import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthRateLimitService {

    private final ConcurrentMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();
    private final AppSettingService appSettingService;
    private final Clock clock;

    @Autowired
    public AuthRateLimitService(AppSettingService appSettingService) {
        this(appSettingService, Clock.systemUTC());
    }

    AuthRateLimitService(AppSettingService appSettingService, Clock clock) {
        this.appSettingService = appSettingService;
        this.clock = clock;
    }

    public void assertAllowed(String username, String clientIp) {
        String key = buildKey(username, clientIp);
        LoginAttempt attempt = attempts.get(key);
        if (attempt == null) {
            return;
        }

        Instant now = clock.instant();
        if (attempt.lockedUntil != null && attempt.lockedUntil.isAfter(now)) {
            throw new BizException(Codes.TOO_MANY_REQUESTS, "too many login attempts, try again later");
        }

        if (attempt.lockedUntil != null || attempt.windowStartedAt.plusSeconds(appSettingService.getLoginWindowSeconds()).isBefore(now)) {
            attempts.remove(key);
        }
    }

    public void recordFailure(String username, String clientIp) {
        String key = buildKey(username, clientIp);
        Instant now = clock.instant();
        int maxAttempts = appSettingService.getLoginMaxAttempts();
        int windowSeconds = appSettingService.getLoginWindowSeconds();

        attempts.compute(key, (ignored, existing) -> {
            LoginAttempt attempt = existing;
            if (attempt == null || attempt.windowStartedAt.plusSeconds(windowSeconds).isBefore(now)) {
                attempt = new LoginAttempt(now, 0, null);
            }

            int failures = attempt.failures + 1;
            Instant lockedUntil = failures >= maxAttempts ? now.plusSeconds(windowSeconds) : attempt.lockedUntil;
            return new LoginAttempt(attempt.windowStartedAt, failures, lockedUntil);
        });
    }

    public void recordSuccess(String username, String clientIp) {
        attempts.remove(buildKey(username, clientIp));
    }

    private String buildKey(String username, String clientIp) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return normalizedUsername + "|" + clientIp;
    }

    private record LoginAttempt(
            Instant windowStartedAt,
            int failures,
            Instant lockedUntil
    ) {
    }
}
