package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.AuthConfigResponse;
import com.mysunriser.backend.dto.AuthConfirmTokenRequest;
import com.mysunriser.backend.dto.AuthLoginRequest;
import com.mysunriser.backend.dto.AuthMeResponse;
import com.mysunriser.backend.dto.AuthMessageResponse;
import com.mysunriser.backend.dto.AuthRegisterRequest;
import com.mysunriser.backend.dto.AuthTokenResponse;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.PasswordForgotRequest;
import com.mysunriser.backend.dto.PasswordResetRequest;
import com.mysunriser.backend.exception.BizException;
import com.mysunriser.backend.service.AccountService;
import com.mysunriser.backend.service.AuthSession;
import com.mysunriser.backend.service.AuthService;
import com.mysunriser.backend.service.ClientIpService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountService accountService;
    private final ClientIpService clientIpService;
    private final String refreshCookieName;
    private final boolean refreshCookieSecure;
    private final long refreshTokenDays;

    public AuthController(
            AuthService authService,
            AccountService accountService,
            ClientIpService clientIpService,
            @Value("${auth.refresh-cookie.name:mysunriser_refresh}") String refreshCookieName,
            @Value("${auth.refresh-cookie.secure:false}") boolean refreshCookieSecure,
            @Value("${auth.refresh-token-days:30}") long refreshTokenDays
    ) {
        this.authService = authService;
        this.accountService = accountService;
        this.clientIpService = clientIpService;
        this.refreshCookieName = refreshCookieName;
        this.refreshCookieSecure = refreshCookieSecure;
        this.refreshTokenDays = refreshTokenDays;
    }

    @PostMapping("/register/request")
    public AuthMessageResponse requestRegistration(
            @Valid @RequestBody AuthRegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.requestRegistration(request, clientIpService.resolve(httpRequest));
        return new AuthMessageResponse("ok");
    }

    @PostMapping("/register/confirm")
    public AuthTokenResponse confirmRegistration(
            @Valid @RequestBody AuthConfirmTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthSession session = authService.confirmRegistration(
                request.token(),
                clientIpService.resolve(httpRequest),
                httpRequest.getHeader(HttpHeaders.USER_AGENT)
        );
        setRefreshCookie(response, session.refreshToken());
        return session.response();
    }

    @PostMapping("/login")
    public AuthTokenResponse login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthSession session = authService.login(
                request,
                clientIpService.resolve(httpRequest),
                httpRequest.getHeader(HttpHeaders.USER_AGENT)
        );
        setRefreshCookie(response, session.refreshToken());
        return session.response();
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(HttpServletRequest httpRequest, HttpServletResponse response) {
        AuthSession session = authService.refresh(
                readRefreshCookie(httpRequest),
                clientIpService.resolve(httpRequest),
                httpRequest.getHeader(HttpHeaders.USER_AGENT)
        );
        setRefreshCookie(response, session.refreshToken());
        return session.response();
    }

    @PostMapping("/password/forgot")
    public AuthMessageResponse forgotPassword(
            @Valid @RequestBody PasswordForgotRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.requestPasswordReset(request, clientIpService.resolve(httpRequest));
        return new AuthMessageResponse("ok");
    }

    @PostMapping("/password/reset")
    public AuthMessageResponse resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return new AuthMessageResponse("ok");
    }

    @PostMapping("/email-change/confirm")
    public AuthMessageResponse confirmEmailChange(@Valid @RequestBody AuthConfirmTokenRequest request) {
        accountService.confirmEmailChange(request.token());
        return new AuthMessageResponse("ok");
    }

    @GetMapping("/config")
    public AuthConfigResponse config() {
        return authService.config();
    }

    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException(Codes.UNAUTHORIZED, "unauthorized");
        }

        return authService.me(authentication.getName());
    }

    @PostMapping("/logout")
    public AuthMessageResponse logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(readRefreshCookie(request));
        clearRefreshCookie(response);
        return new AuthMessageResponse("ok");
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(refreshTokenDays))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }

        for (Cookie cookie : cookies) {
            if (refreshCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return "";
    }
}
