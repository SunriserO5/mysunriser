package com.mysunriser.backend.dto;

public record AuthTokenResponse(
        String token,
        long expireMinutes,
        String username,
        String role
) {
}
