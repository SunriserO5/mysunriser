package com.mysunriser.backend.dto;

import jakarta.validation.constraints.Size;

public record FooterSettingsRequest(
        boolean githubEnabled,

        @Size(max = 255, message = "githubUrl must be at most 255 characters")
        String githubUrl,

        boolean xEnabled,

        @Size(max = 255, message = "xUrl must be at most 255 characters")
        String xUrl
) {
}
