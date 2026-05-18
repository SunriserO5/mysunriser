package com.mysunriser.backend.dto;

import java.util.Locale;

public enum ToolAccessLevel {
    PUBLIC,
    AUTHENTICATED,
    ADMIN;

    public static ToolAccessLevel from(String value) {
        if (value == null || value.isBlank()) {
            return PUBLIC;
        }

        return ToolAccessLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
