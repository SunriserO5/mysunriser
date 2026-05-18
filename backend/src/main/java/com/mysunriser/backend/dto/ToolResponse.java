package com.mysunriser.backend.dto;

import com.mysunriser.backend.entity.OnlineTool;

import java.time.LocalDateTime;

public record ToolResponse(
        Long id,
        String slug,
        String title,
        String summary,
        String status,
        String entryType,
        String routePath,
        String externalUrl,
        String accessLevel,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ToolResponse of(OnlineTool tool) {
        return new ToolResponse(
                tool.getId(),
                tool.getSlug(),
                tool.getTitle(),
                tool.getSummary(),
                tool.getStatus(),
                tool.getEntryType(),
                tool.getRoutePath(),
                tool.getExternalUrl(),
                tool.getAccessLevel(),
                tool.getSortOrder(),
                tool.getCreatedAt(),
                tool.getUpdatedAt()
        );
    }
}
