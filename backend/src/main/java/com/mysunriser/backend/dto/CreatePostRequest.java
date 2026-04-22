package com.mysunriser.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysunriser.backend.entity.post;

import java.time.LocalDateTime;

public class CreatePostRequest {
    private String slug;
    private String title;
    private String content;
    private String status;

    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    public post toEntity() {
        post postEntity = new post();
        postEntity.setSlug(slug);
        postEntity.setTitle(title);
        postEntity.setContent(content);
        postEntity.setStatus(status);
        postEntity.setPublished_at(publishedAt == null ? LocalDateTime.now() : publishedAt);
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