package com.mysunriser.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String appBaseUrl;

    public EmailNotificationService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${auth.mail.from:}") String from,
            @Value("${app.base-url:http://localhost:5173}") String appBaseUrl
    ) {
        this.mailSender = mailSender.getIfAvailable();
        this.from = from == null ? "" : from.trim();
        this.appBaseUrl = trimTrailingSlash(appBaseUrl);
    }

    public void sendRegistrationConfirmation(String email, String token) {
        String link = appBaseUrl + "/register/confirm?token=" + encode(token);
        sendOrLog(email, "确认你的 MySunriser 账号", "请打开下面的链接完成注册：\n\n" + link, link);
    }

    public void sendPasswordReset(String email, String token) {
        String link = appBaseUrl + "/password/reset?token=" + encode(token);
        sendOrLog(email, "重置你的 MySunriser 密码", "请打开下面的链接重置密码：\n\n" + link, link);
    }

    public void sendEmailChangeConfirmation(String email, String token) {
        String link = appBaseUrl + "/account/email/confirm?token=" + encode(token);
        sendOrLog(email, "确认新的 MySunriser 邮箱", "请打开下面的链接确认邮箱变更：\n\n" + link, link);
    }

    private void sendOrLog(String to, String subject, String text, String fallbackLink) {
        if (mailSender == null || from.isBlank()) {
            log.info("Mail is not configured; {} link for {}: {}", subject, to, fallbackLink);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private String encode(String token) {
        return URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String trimTrailingSlash(String value) {
        String normalized = value == null || value.isBlank() ? "http://localhost:5173" : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
