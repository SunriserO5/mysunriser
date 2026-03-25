package com.mysunriser.backend.dto;

public class PostResponse {
    private int postId;
    private String slug;
    private String title;
    private String content;
    private String status;
    private int publishTime;

    private PostResponse(int postId, String slug, String title, String content, String status, int publishTime) {
        this.postId = postId;
        this.slug = slug;
        this.title = title;
        this.content = content;
        this.status = status;
        this.publishTime = publishTime;
    }
    public static PostResponse of(){
        return null;
    }

    public int getPostId() {
        return postId;
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


