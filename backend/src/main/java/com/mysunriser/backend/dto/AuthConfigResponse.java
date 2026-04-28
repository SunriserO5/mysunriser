package com.mysunriser.backend.dto;

public record AuthConfigResponse(
        boolean registrationEnabled,
        boolean turnstileEnabled,
        String turnstileSiteKey
) {
}
