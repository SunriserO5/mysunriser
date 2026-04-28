package com.mysunriser.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class TurnstileService {

    private static final URI SITE_VERIFY_URI = URI.create("https://challenges.cloudflare.com/turnstile/v0/siteverify");

    private final AppSettingService appSettingService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public TurnstileService(AppSettingService appSettingService) {
        this(appSettingService, HttpClient.newHttpClient(), new ObjectMapper());
    }

    TurnstileService(AppSettingService appSettingService, HttpClient httpClient, ObjectMapper objectMapper) {
        this.appSettingService = appSettingService;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public void verify(String token, String clientIp) {
        if (!appSettingService.isTurnstileEnabled()) {
            return;
        }

        if (!appSettingService.isTurnstileConfigured()) {
            throw new BizException(Codes.FORBIDDEN, "turnstile is not configured");
        }

        if (token == null || token.isBlank()) {
            throw new BizException(Codes.FORBIDDEN, "turnstile verification is required");
        }

        HttpRequest request = HttpRequest.newBuilder(SITE_VERIFY_URI)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(buildBody(token, clientIp)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400 || !isSuccess(response.body())) {
                throw new BizException(Codes.FORBIDDEN, "turnstile verification failed");
            }
        } catch (IOException e) {
            throw new BizException(Codes.FORBIDDEN, "turnstile verification failed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(Codes.FORBIDDEN, "turnstile verification failed");
        }
    }

    private boolean isSuccess(String body) throws IOException {
        JsonNode json = objectMapper.readTree(body);
        return json.path("success").asBoolean(false);
    }

    private String buildBody(String token, String clientIp) {
        return "secret=" + encode(appSettingService.getTurnstileSecretKey())
                + "&response=" + encode(token)
                + "&remoteip=" + encode(clientIp);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
