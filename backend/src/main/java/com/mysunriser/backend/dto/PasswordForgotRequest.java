package com.mysunriser.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordForgotRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 254, message = "email length must be at most 254")
        String email
) {
}
