package com.mysunriser.backend.dto;

public record FooterSettingsResponse(
        boolean githubEnabled,
        String githubUrl,
        boolean xEnabled,
        String xUrl
) {
}
