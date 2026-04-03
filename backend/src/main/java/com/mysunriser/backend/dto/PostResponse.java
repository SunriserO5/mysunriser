package com.mysunriser.backend.dto;

import java.time.LocalDateTime;

import com.mysunriser.backend.entity.post;

public class PostResponse {
    private int id;
    private String slug;
    private String title;
    private String content;
    private String status;
    private LocalDateTime publish_at;

    private PostResponse(int postId, String slug, String title, String content, String status, LocalDateTime published_at) {
        this.id = postId;
        this.slug = slug;
        this.title = title;
        this.content = content;
        this.status = status;
        this.publish_at = published_at;
    }
    public static PostResponse of(post postEntity){
        return new PostResponse(
                postEntity.getId(),
                postEntity.getSlug(),
                postEntity.getTitle(),
                postEntity.getContent(),
                postEntity.getStatus(),
                postEntity.getPublished_at()
        );
    }

    public int getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getPublish_at() {
        return publish_at;
    }
}


