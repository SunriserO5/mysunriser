package com.mysunriser.backend.dto;

import java.time.LocalDateTime;

public record AdminUserItemResponse(
        Long id,
        String username,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt
) {
}
