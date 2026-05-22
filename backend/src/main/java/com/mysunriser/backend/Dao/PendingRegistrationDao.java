package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.entity.PendingRegistration;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PendingRegistrationDao extends BaseMapper<PendingRegistration> {

    @Delete("DELETE FROM pending_registration WHERE username = #{username} OR email = #{email}")
    void deleteByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
}
