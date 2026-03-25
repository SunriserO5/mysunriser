package com.mysunriser.backend.dto;

import com.mysunriser.backend.entity.post;

public class PostResponse {
    private int id;
    private String slug;
    private String title;
    private String content;
    private String status;
    private int publishTime;

    private PostResponse(int postId, String slug, String title, String content, String status, int publishTime) {
        this.id = postId;
        this.slug = slug;
        this.title = title;
        this.content = content;
        this.status = status;
        this.publishTime = publishTime;
    }
    public static PostResponse of(post postEntity){
        return new PostResponse(
                postEntity.getId(),
                postEntity.getSlug(),
                postEntity.getTitle(),
                postEntity.getContent(),
                postEntity.getStatus(),
                postEntity.getPublishTime()
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

    public int getPublishTime() {
        return publishTime;
    }
}


