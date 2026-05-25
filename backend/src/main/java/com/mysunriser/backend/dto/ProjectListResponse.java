package com.mysunriser.backend.dto;

import java.util.List;

public record ProjectListResponse(
        int page,
        int pageSize,
        long total,
        List<ProjectResponse> items
) {
}
