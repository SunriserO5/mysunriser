package com.mysunriser.backend.dto;

public record AuthMeResponse(
        String username,
        String role,
        String status
) {
}
