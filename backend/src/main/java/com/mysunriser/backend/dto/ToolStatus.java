package com.mysunriser.backend.dto;

import java.util.Locale;

public enum ToolStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String value;

    ToolStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ToolStatus from(String value) {
        if (value == null || value.isBlank()) {
            return DRAFT;
        }

        return ToolStatus.valueOf(value.trim().replace('-', '_').toUpperCase(Locale.ROOT));
    }
}
