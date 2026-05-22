package com.mysunriser.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 32, message = "username length must be between 3 and 32")
        String username,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 254, message = "email length must be at most 254")
        String email,

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
