package com.mysunriser.backend.dto;

public record AccountProfileResponse(
        Long id,
        String username,
        String role,
        String status,
        String email,
        boolean emailVerified,
        String nickname,
        String avatarUrl
) {
}
