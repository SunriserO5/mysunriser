package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.AdminAuthConfigRequest;
import com.mysunriser.backend.dto.AuthConfigResponse;
import com.mysunriser.backend.service.AppSettingService;
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
        return new AuthConfigResponse(appSettingService.isRegistrationEnabled());
    }

    @PutMapping("/auth")
    public AuthConfigResponse updateAuthConfig(@RequestBody AdminAuthConfigRequest request) {
        boolean registrationEnabled = appSettingService.updateRegistrationEnabled(request.registrationEnabled());
        return new AuthConfigResponse(registrationEnabled);
    }
}
