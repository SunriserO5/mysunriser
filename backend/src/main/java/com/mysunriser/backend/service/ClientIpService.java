package com.mysunriser.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ClientIpService {

    public String resolve(HttpServletRequest request) {
        String cloudflareIp = trimToNull(request.getHeader("CF-Connecting-IP"));
        if (cloudflareIp != null) {
            return cloudflareIp;
        }

        String forwardedFor = trimToNull(request.getHeader("X-Forwarded-For"));
        if (forwardedFor != null) {
            int commaIndex = forwardedFor.indexOf(',');
            return commaIndex >= 0 ? forwardedFor.substring(0, commaIndex).trim() : forwardedFor;
        }

        return request.getRemoteAddr();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

