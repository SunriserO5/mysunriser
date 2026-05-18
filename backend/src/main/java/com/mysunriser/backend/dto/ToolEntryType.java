package com.mysunriser.backend.dto;

import java.util.Locale;

public enum ToolEntryType {
    INTERNAL,
    EXTERNAL;

    public static ToolEntryType from(String value) {
        if (value == null || value.isBlank()) {
            return INTERNAL;
        }

        return ToolEntryType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
