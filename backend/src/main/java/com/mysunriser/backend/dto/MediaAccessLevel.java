package com.mysunriser.backend.dto;

import java.util.Locale;

public enum MediaAccessLevel {
    PUBLIC,
    AUTHENTICATED;

    public static MediaAccessLevel from(String value) {
        if (value == null || value.isBlank()) {
            return PUBLIC;
        }

        return MediaAccessLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
