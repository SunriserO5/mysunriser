package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.FooterSettingsResponse;
import com.mysunriser.backend.service.AppSettingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SiteSettingController {

    private final AppSettingService appSettingService;

    public SiteSettingController(AppSettingService appSettingService) {
        this.appSettingService = appSettingService;
    }

    @GetMapping("/footer")
    public FooterSettingsResponse getFooterSettings() {
        return appSettingService.getFooterSettings();
    }
}
