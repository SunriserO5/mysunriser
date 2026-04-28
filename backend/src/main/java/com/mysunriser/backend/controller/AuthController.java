package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.AuthConfigResponse;
import com.mysunriser.backend.dto.AuthLoginRequest;
import com.mysunriser.backend.dto.AuthMeResponse;
import com.mysunriser.backend.dto.AuthRegisterRequest;
import com.mysunriser.backend.dto.AuthTokenResponse;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.exception.BizException;
import com.mysunriser.backend.service.AuthService;
import com.mysunriser.backend.service.ClientIpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ClientIpService clientIpService;

    public AuthController(AuthService authService, ClientIpService clientIpService) {
        this.authService = authService;
        this.clientIpService = clientIpService;
    }

    @PostMapping("/register")
    public AuthTokenResponse register(@Valid @RequestBody AuthRegisterRequest request, HttpServletRequest httpRequest) {
        return authService.register(request, clientIpService.resolve(httpRequest));
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody AuthLoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, clientIpService.resolve(httpRequest));
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
    public Map<String, String> logout() {
        return Map.of("message", "ok");
    }
}
