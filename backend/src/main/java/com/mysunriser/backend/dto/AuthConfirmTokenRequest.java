package com.mysunriser.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthConfirmTokenRequest(
        @NotBlank(message = "token is required")
        String token
) {
}
