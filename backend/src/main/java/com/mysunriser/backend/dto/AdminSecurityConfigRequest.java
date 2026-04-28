package com.mysunriser.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AdminSecurityConfigRequest(
        boolean registrationEnabled,
        boolean turnstileEnabled,

        @Min(value = 3, message = "loginMaxAttempts must be at least 3")
        @Max(value = 20, message = "loginMaxAttempts must be at most 20")
        int loginMaxAttempts,

        @Min(value = 60, message = "loginWindowSeconds must be at least 60")
        @Max(value = 3600, message = "loginWindowSeconds must be at most 3600")
        int loginWindowSeconds
) {
}

