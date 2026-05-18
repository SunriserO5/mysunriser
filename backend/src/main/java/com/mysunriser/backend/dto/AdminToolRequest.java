package com.mysunriser.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminToolRequest(
        @NotBlank(message = "slug is required")
        @Pattern(regexp = "^[a-z0-9][a-z0-9-]{0,99}$", message = "slug must use lowercase letters, numbers, and hyphens")
        String slug,

        @NotBlank(message = "title is required")
        @Size(max = 120, message = "title must be at most 120 characters")
        String title,

        @NotBlank(message = "summary is required")
        @Size(max = 500, message = "summary must be at most 500 characters")
        String summary,

        String status,

        String entryType,

        @Size(max = 255, message = "routePath must be at most 255 characters")
        String routePath,

        @Size(max = 500, message = "externalUrl must be at most 500 characters")
        String externalUrl,

        String accessLevel,

        @Min(value = -100000, message = "sortOrder is too small")
        @Max(value = 100000, message = "sortOrder is too large")
        Integer sortOrder
) {
}
