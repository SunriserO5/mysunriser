package com.mysunriser.backend.dto;

import jakarta.validation.constraints.Size;

public record AccountProfileUpdateRequest(
        @Size(max = 64, message = "nickname length must be at most 64")
        String nickname
) {
}
