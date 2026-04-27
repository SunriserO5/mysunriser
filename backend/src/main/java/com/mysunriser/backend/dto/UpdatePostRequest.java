package com.mysunriser.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdatePostRequest(
        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @NotBlank(message = "content is required")
        String content,

        @NotBlank(message = "status is required")
        @Size(max = 20, message = "status must be at most 20 characters")
        String status,

        @JsonProperty("published_at")
        LocalDateTime publishedAt
) {
}
