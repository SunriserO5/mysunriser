package com.mysunriser.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysunriser.backend.entity.post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreatePostRequest {
    @NotBlank(message = "slug is required")
    @Size(max = 128, message = "slug must be at most 128 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug must use lowercase letters, numbers, and hyphens")
    private String slug;

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be at most 200 characters")
    private String title;

    @NotBlank(message = "content is required")
    private String content;

    @NotBlank(message = "status is required")
    @Size(max = 20, message = "status must be at most 20 characters")
    private String status;

    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    public post toEntity() {
        return toEntity(true);
    }

    public post toCreateEntity() {
        return toEntity(false);
    }

    private post toEntity(boolean defaultPublishedAt) {
        post postEntity = new post();
        postEntity.setSlug(slug.trim());
        postEntity.setTitle(title.trim());
        postEntity.setContent(content);
        postEntity.setStatus(status.trim());
        postEntity.setPublished_at(defaultPublishedAt && publishedAt == null ? LocalDateTime.now() : publishedAt);
        return postEntity;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
