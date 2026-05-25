package com.mysunriser.backend.dto;

import java.util.Locale;

public enum ProjectStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String value;

    ProjectStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ProjectStatus from(String value) {
        if (value == null || value.isBlank()) {
            return DRAFT;
        }

        return ProjectStatus.valueOf(value.trim().replace('-', '_').toUpperCase(Locale.ROOT));
    }
}
