package com.mysunriser.backend.dto;

public record AdminSecurityConfigResponse(
        boolean registrationEnabled,
        boolean turnstileEnabled,
        boolean turnstileConfigured,
        String turnstileSiteKey,
        int loginMaxAttempts,
        int loginWindowSeconds,
        long jwtExpireMinutes
) {
}

