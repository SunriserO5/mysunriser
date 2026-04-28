package com.mysunriser.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
        @NotBlank(message = "newPassword is required")
        @Size(min = 10, max = 72, message = "newPassword length must be between 10 and 72")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "newPassword must include letters and numbers"
        )
        String newPassword
) {
}
