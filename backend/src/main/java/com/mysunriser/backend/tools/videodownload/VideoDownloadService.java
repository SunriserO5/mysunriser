package com.mysunriser.backend.tools.videodownload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoDownloadService {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String extractUrl;
    private final String clientId;
    private final String clientSecret;

    public VideoDownloadService(
            @Value("${tools.video-download.iiilab.extract-url:https://service.iiilab.com/openapi/extract}") String extractUrl,
            @Value("${tools.video-download.iiilab.client-id:}") String clientId,
            @Value("${tools.video-download.iiilab.client-secret:}") String clientSecret
    ) {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.extractUrl = extractUrl == null ? "" : extractUrl.trim();
        this.clientId = clientId == null ? "" : clientId.trim();
        this.clientSecret = clientSecret == null ? "" : clientSecret.trim();
    }

    public VideoDownloadExtractResponse extract(VideoDownloadExtractRequest request) {
        URI sourceUri = normalizeSourceUrl(request.url());
        URI apiUri = normalizeExtractUrl();
        assertConfigured();

        try {
            String body = objectMapper.writeValueAsString(Map.of("url", sourceUri.toString()));
            HttpRequest httpRequest = HttpRequest.newBuilder(apiUri)
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("x-client-id", clientId)
                    .header("x-client-secret", clientSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BizException(Codes.VALIDATION_ERROR, errorMessage(response.body(), response.statusCode()));
            }

            return parseResponse(response.body());
        } catch (BizException e) {
            throw e;
        } catch (IOException e) {
            throw new BizException(Codes.INTERNAL_ERROR, "video extract service unavailable");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(Codes.INTERNAL_ERROR, "video extract request interrupted");
        }
    }

    private void assertConfigured() {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new BizException(Codes.INTERNAL_ERROR, "video download api credentials are not configured");
        }
    }

    private URI normalizeSourceUrl(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new BizException(Codes.VALIDATION_ERROR, "url is required");
        }

        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new BizException(Codes.VALIDATION_ERROR, "url must be an http or https URL");
            }

            return uri;
        } catch (URISyntaxException e) {
            throw new BizException(Codes.VALIDATION_ERROR, "url format is invalid");
        }
    }

    private URI normalizeExtractUrl() {
        try {
            URI uri = new URI(extractUrl);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new BizException(Codes.INTERNAL_ERROR, "video download api URL is invalid");
            }

            return uri;
        } catch (URISyntaxException e) {
            throw new BizException(Codes.INTERNAL_ERROR, "video download api URL is invalid");
        }
    }

    private VideoDownloadExtractResponse parseResponse(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        List<VideoDownloadExtractResponse.Media> medias = new ArrayList<>();
        JsonNode mediaNodes = root.path("medias");
        if (mediaNodes.isArray()) {
            for (JsonNode mediaNode : mediaNodes) {
                medias.add(parseMedia(mediaNode));
            }
        }

        return new VideoDownloadExtractResponse(text(root, "text"), medias);
    }

    private VideoDownloadExtractResponse.Media parseMedia(JsonNode node) {
        List<VideoDownloadExtractResponse.Format> formats = new ArrayList<>();
        JsonNode formatNodes = node.path("formats");
        if (formatNodes.isArray()) {
            for (JsonNode formatNode : formatNodes) {
                formats.add(new VideoDownloadExtractResponse.Format(
                        integer(formatNode, "quality"),
                        text(formatNode, "video_url"),
                        text(formatNode, "video_ext"),
                        longValue(formatNode, "video_size"),
                        text(formatNode, "audio_url"),
                        text(formatNode, "audio_ext"),
                        longValue(formatNode, "audio_size"),
                        integer(formatNode, "separate"),
                        text(formatNode, "quality_note")
                ));
            }
        }

        return new VideoDownloadExtractResponse.Media(
                text(node, "media_type"),
                text(node, "resource_url"),
                text(node, "preview_url"),
                formats,
                parseHeaders(node.path("headers"))
        );
    }

    private Map<String, String> parseHeaders(JsonNode headersNode) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (!headersNode.isObject()) {
            return headers;
        }

        headersNode.properties().forEach(entry -> headers.put(entry.getKey(), entry.getValue().asText("")));
        return headers;
    }

    private String errorMessage(String body, int statusCode) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String message = text(root, "message");
            if (message != null && !message.isBlank()) {
                return message;
            }
        } catch (RuntimeException | IOException ignored) {
        }

        return "video extract failed with status " + statusCode;
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }

        return value.asText();
    }

    private Integer integer(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }

        return value.isNumber() ? value.intValue() : null;
    }

    private Long longValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }

        return value.isNumber() ? value.longValue() : null;
    }
}
