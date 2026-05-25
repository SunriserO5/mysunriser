package com.mysunriser.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminProjectRequest(
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

        @NotBlank(message = "repoOwner is required")
        @Pattern(regexp = "^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$", message = "repoOwner must be a valid GitHub owner")
        String repoOwner,

        @NotBlank(message = "repoName is required")
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,120}$", message = "repoName must be a valid GitHub repository name")
        String repoName,

        @Size(max = 500, message = "repoUrl must be at most 500 characters")
        String repoUrl,

        @Min(value = -100000, message = "sortOrder is too small")
        @Max(value = 100000, message = "sortOrder is too large")
        Integer sortOrder
) {
}
