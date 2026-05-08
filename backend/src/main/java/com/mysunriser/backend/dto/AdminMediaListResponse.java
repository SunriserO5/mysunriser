package com.mysunriser.backend.dto;

import java.util.List;

public record AdminMediaListResponse(
        int page,
        int pageSize,
        long total,
        List<MediaAssetResponse> items
) {
}
