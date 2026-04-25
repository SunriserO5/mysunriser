package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.entity.AppSetting;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppSettingDao extends BaseMapper<AppSetting> {

    @Insert("""
            INSERT INTO app_setting(setting_key, setting_value)
            VALUES (#{key}, #{value})
            ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)
            """)
    void upsert(@Param("key") String key, @Param("value") String value);
}
