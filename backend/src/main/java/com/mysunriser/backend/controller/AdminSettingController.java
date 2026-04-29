package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.AdminAuthConfigRequest;
import com.mysunriser.backend.dto.AdminSecurityConfigRequest;
import com.mysunriser.backend.dto.AdminSecurityConfigResponse;
import com.mysunriser.backend.dto.AuthConfigResponse;
import com.mysunriser.backend.dto.FooterSettingsRequest;
import com.mysunriser.backend.dto.FooterSettingsResponse;
import com.mysunriser.backend.service.AppSettingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingController {

    private final AppSettingService appSettingService;

    public AdminSettingController(AppSettingService appSettingService) {
        this.appSettingService = appSettingService;
    }

    @GetMapping("/auth")
    public AuthConfigResponse getAuthConfig() {
        return appSettingService.getPublicAuthConfig();
    }

    @PutMapping("/auth")
    public AuthConfigResponse updateAuthConfig(@RequestBody AdminAuthConfigRequest request) {
        boolean registrationEnabled = appSettingService.updateRegistrationEnabled(request.registrationEnabled());
        return new AuthConfigResponse(
                registrationEnabled,
                appSettingService.isTurnstileEnabled() && appSettingService.isTurnstileConfigured(),
                appSettingService.getTurnstileSiteKey()
        );
    }

    @GetMapping("/security")
    public AdminSecurityConfigResponse getSecurityConfig() {
        return appSettingService.getSecurityConfig();
    }

    @PutMapping("/security")
    public AdminSecurityConfigResponse updateSecurityConfig(@Valid @RequestBody AdminSecurityConfigRequest request) {
        return appSettingService.updateSecurityConfig(request);
    }

    @GetMapping("/footer")
    public FooterSettingsResponse getFooterSettings() {
        return appSettingService.getFooterSettings();
    }

    @PutMapping("/footer")
    public FooterSettingsResponse updateFooterSettings(@Valid @RequestBody FooterSettingsRequest request) {
        return appSettingService.updateFooterSettings(request);
    }
}
