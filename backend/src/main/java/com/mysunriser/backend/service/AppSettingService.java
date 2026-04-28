package com.mysunriser.backend.service;

import com.mysunriser.backend.Dao.AppSettingDao;
import com.mysunriser.backend.dto.AdminSecurityConfigRequest;
import com.mysunriser.backend.dto.AdminSecurityConfigResponse;
import com.mysunriser.backend.dto.AuthConfigResponse;
import com.mysunriser.backend.entity.AppSetting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppSettingService {

    private static final String REGISTRATION_ENABLED_KEY = "auth.registration-enabled";
    private static final String TURNSTILE_ENABLED_KEY = "security.turnstile-enabled";
    private static final String LOGIN_MAX_ATTEMPTS_KEY = "security.login-max-attempts";
    private static final String LOGIN_WINDOW_SECONDS_KEY = "security.login-window-seconds";

    private final AppSettingDao appSettingDao;
    private final boolean defaultRegistrationEnabled;
    private final boolean defaultTurnstileEnabled;
    private final String turnstileSiteKey;
    private final String turnstileSecretKey;
    private final int defaultLoginMaxAttempts;
    private final int defaultLoginWindowSeconds;
    private final long jwtExpireMinutes;

    public AppSettingService(
            AppSettingDao appSettingDao,
            @Value("${auth.registration-enabled:false}") boolean defaultRegistrationEnabled,
            @Value("${turnstile.enabled:false}") boolean defaultTurnstileEnabled,
            @Value("${turnstile.site-key:}") String turnstileSiteKey,
            @Value("${turnstile.secret-key:}") String turnstileSecretKey,
            @Value("${security.login.max-attempts:5}") int defaultLoginMaxAttempts,
            @Value("${security.login.window-seconds:900}") int defaultLoginWindowSeconds,
            @Value("${jwt.expireMinutes}") long jwtExpireMinutes
    ) {
        this.appSettingDao = appSettingDao;
        this.defaultRegistrationEnabled = defaultRegistrationEnabled;
        this.defaultTurnstileEnabled = defaultTurnstileEnabled;
        this.turnstileSiteKey = turnstileSiteKey == null ? "" : turnstileSiteKey.trim();
        this.turnstileSecretKey = turnstileSecretKey == null ? "" : turnstileSecretKey.trim();
        this.defaultLoginMaxAttempts = defaultLoginMaxAttempts;
        this.defaultLoginWindowSeconds = defaultLoginWindowSeconds;
        this.jwtExpireMinutes = jwtExpireMinutes;
    }

    public boolean isRegistrationEnabled() {
        return getBoolean(REGISTRATION_ENABLED_KEY, defaultRegistrationEnabled);
    }

    public boolean updateRegistrationEnabled(boolean enabled) {
        appSettingDao.upsert(REGISTRATION_ENABLED_KEY, Boolean.toString(enabled));
        return enabled;
    }

    public boolean isTurnstileEnabled() {
        return getBoolean(TURNSTILE_ENABLED_KEY, defaultTurnstileEnabled);
    }

    public boolean isTurnstileConfigured() {
        return !turnstileSiteKey.isBlank() && !turnstileSecretKey.isBlank();
    }

    public String getTurnstileSiteKey() {
        return turnstileSiteKey;
    }

    public String getTurnstileSecretKey() {
        return turnstileSecretKey;
    }

    public int getLoginMaxAttempts() {
        return getInt(LOGIN_MAX_ATTEMPTS_KEY, defaultLoginMaxAttempts);
    }

    public int getLoginWindowSeconds() {
        return getInt(LOGIN_WINDOW_SECONDS_KEY, defaultLoginWindowSeconds);
    }

    public AuthConfigResponse getPublicAuthConfig() {
        boolean effectiveTurnstileEnabled = isTurnstileEnabled() && isTurnstileConfigured();
        return new AuthConfigResponse(
                isRegistrationEnabled(),
                effectiveTurnstileEnabled,
                effectiveTurnstileEnabled ? turnstileSiteKey : ""
        );
    }

    public AdminSecurityConfigResponse getSecurityConfig() {
        return new AdminSecurityConfigResponse(
                isRegistrationEnabled(),
                isTurnstileEnabled(),
                isTurnstileConfigured(),
                turnstileSiteKey,
                getLoginMaxAttempts(),
                getLoginWindowSeconds(),
                jwtExpireMinutes
        );
    }

    public AdminSecurityConfigResponse updateSecurityConfig(AdminSecurityConfigRequest request) {
        appSettingDao.upsert(REGISTRATION_ENABLED_KEY, Boolean.toString(request.registrationEnabled()));
        appSettingDao.upsert(TURNSTILE_ENABLED_KEY, Boolean.toString(request.turnstileEnabled()));
        appSettingDao.upsert(LOGIN_MAX_ATTEMPTS_KEY, Integer.toString(request.loginMaxAttempts()));
        appSettingDao.upsert(LOGIN_WINDOW_SECONDS_KEY, Integer.toString(request.loginWindowSeconds()));
        return getSecurityConfig();
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        AppSetting setting = appSettingDao.selectById(key);

        if (setting == null || setting.getSettingValue() == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(setting.getSettingValue());
    }

    private int getInt(String key, int defaultValue) {
        AppSetting setting = appSettingDao.selectById(key);

        if (setting == null || setting.getSettingValue() == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(setting.getSettingValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
