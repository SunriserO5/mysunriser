package com.mysunriser.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class GitHubReadmeService {
    private static final String[] README_FILENAMES = {"README.md", "readme.md", "README.MD", "README"};
    private static final String[] README_REFS = {"HEAD", "main", "master"};

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String githubToken;

    @Autowired
    public GitHubReadmeService(@Value("${github.token:}") String githubToken) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), new ObjectMapper(), githubToken);
    }

    GitHubReadmeService(HttpClient httpClient, ObjectMapper objectMapper, String githubToken) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.githubToken = githubToken == null ? "" : githubToken.trim();
    }

    public GitHubReadmeResult fetchReadme(String owner, String repo, String etag) {
        URI uri = URI.create("https://api.github.com/repos/%s/%s/readme".formatted(url(owner), url(repo)));
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "mysunriser-project-readme");

        if (etag != null && !etag.isBlank()) {
            builder.header("If-None-Match", etag);
        }

        if (!githubToken.isBlank()) {
            builder.header("Authorization", "Bearer " + githubToken);
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.GET().build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 304) {
                return GitHubReadmeResult.notModified();
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                if (response.statusCode() == 404) {
                    GitHubReadmeResult fallback = fetchRawReadme(owner, repo);
                    if (fallback.fetched()) {
                        return fallback;
                    }
                }

                return GitHubReadmeResult.failed(errorMessage(response.statusCode()));
            }

            String responseEtag = response.headers().firstValue("ETag").orElse("");
            return GitHubReadmeResult.fetched(parseMarkdown(response.body()), responseEtag);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return GitHubReadmeResult.failed("README 获取请求已中断");
        } catch (IOException | RuntimeException e) {
            return GitHubReadmeResult.failed("README 暂时无法获取");
        }
    }

    private GitHubReadmeResult fetchRawReadme(String owner, String repo) {
        for (String ref : README_REFS) {
            for (String filename : README_FILENAMES) {
                URI uri = URI.create("https://raw.githubusercontent.com/%s/%s/%s/%s".formatted(
                        url(owner),
                        url(repo),
                        url(ref),
                        url(filename)
                ));

                try {
                    HttpResponse<String> response = httpClient.send(
                            baseRequest(uri)
                                    .header("Accept", "text/plain")
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                    );

                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return GitHubReadmeResult.fetched(response.body(), "");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return GitHubReadmeResult.failed("README 获取请求已中断");
                } catch (IOException | RuntimeException ignored) {
                }
            }
        }

        return GitHubReadmeResult.failed(errorMessage(404));
    }

    private HttpRequest.Builder baseRequest(URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "mysunriser-project-readme");

        if (!githubToken.isBlank()) {
            builder.header("Authorization", "Bearer " + githubToken);
        }

        return builder;
    }

    private String parseMarkdown(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        String encoding = root.path("encoding").asText("");
        String content = root.path("content").asText("");

        if (!"base64".equalsIgnoreCase(encoding) || content.isBlank()) {
            return "";
        }

        byte[] decoded = Base64.getMimeDecoder().decode(content);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private String errorMessage(int statusCode) {
        return switch (statusCode) {
            case 404 -> "仓库不可访问，或 README 未在默认分支根目录找到";
            case 403, 429 -> "GitHub 请求受限，请稍后再试";
            default -> "README 获取失败，GitHub 返回状态 " + statusCode;
        };
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
