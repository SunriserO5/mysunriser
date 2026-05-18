package com.mysunriser.backend.dto;

import java.util.List;

public record ToolListResponse(
        int page,
        int pageSize,
        long total,
        List<ToolResponse> items
) {
}
