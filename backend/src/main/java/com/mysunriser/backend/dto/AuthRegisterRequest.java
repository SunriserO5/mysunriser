package com.mysunriser.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 32, message = "username length must be between 3 and 32")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 10, max = 72, message = "password length must be between 10 and 72")
        @jakarta.validation.constraints.Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "password must include letters and numbers"
        )
        String password,

        String turnstileToken
) {
}
