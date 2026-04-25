package com.mysunriser.backend.service;

import com.mysunriser.backend.Dao.AppSettingDao;
import com.mysunriser.backend.entity.AppSetting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppSettingService {

    private static final String REGISTRATION_ENABLED_KEY = "auth.registration-enabled";

    private final AppSettingDao appSettingDao;
    private final boolean defaultRegistrationEnabled;

    public AppSettingService(
            AppSettingDao appSettingDao,
            @Value("${auth.registration-enabled:false}") boolean defaultRegistrationEnabled
    ) {
        this.appSettingDao = appSettingDao;
        this.defaultRegistrationEnabled = defaultRegistrationEnabled;
    }

    public boolean isRegistrationEnabled() {
        AppSetting setting = appSettingDao.selectById(REGISTRATION_ENABLED_KEY);

        if (setting == null || setting.getSettingValue() == null) {
            return defaultRegistrationEnabled;
        }

        return Boolean.parseBoolean(setting.getSettingValue());
    }

    public boolean updateRegistrationEnabled(boolean enabled) {
        appSettingDao.upsert(REGISTRATION_ENABLED_KEY, Boolean.toString(enabled));
        return enabled;
    }
}
