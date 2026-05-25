package com.mysunriser.backend.service;

public record GitHubReadmeResult(
        boolean fetched,
        String markdown,
        String etag,
        String error
) {
    public static GitHubReadmeResult fetched(String markdown, String etag) {
        return new GitHubReadmeResult(true, markdown, etag, null);
    }

    public static GitHubReadmeResult notModified() {
        return new GitHubReadmeResult(false, null, null, null);
    }

    public static GitHubReadmeResult failed(String error) {
        return new GitHubReadmeResult(false, null, null, error);
    }
}
