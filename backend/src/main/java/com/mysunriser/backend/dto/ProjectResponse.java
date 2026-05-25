package com.mysunriser.backend.dto;

import com.mysunriser.backend.entity.Project;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String slug,
        String title,
        String summary,
        String status,
        String repoOwner,
        String repoName,
        String repoUrl,
        Integer sortOrder,
        String readmeMarkdown,
        String readmeError,
        LocalDateTime readmeCachedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponse of(Project project) {
        return of(project, project.getReadmeMarkdown(), null);
    }

    public static ProjectResponse of(Project project, String readmeMarkdown, String readmeError) {
        return new ProjectResponse(
                project.getId(),
                project.getSlug(),
                project.getTitle(),
                project.getSummary(),
                project.getStatus(),
                project.getRepoOwner(),
                project.getRepoName(),
                project.getRepoUrl(),
                project.getSortOrder(),
                readmeMarkdown == null ? "" : readmeMarkdown,
                readmeError == null ? "" : readmeError,
                project.getReadmeCachedAt(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public static ProjectResponse listItem(Project project) {
        return of(project, "", null);
    }
}
