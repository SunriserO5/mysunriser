package com.mysunriser.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
        @NotBlank(message = "newPassword is required")
        @Size(min = 6, max = 72, message = "newPassword length must be between 6 and 72")
        String newPassword
) {
}
