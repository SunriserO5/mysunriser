package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao extends BaseMapper<UserAccount> {

    @Select("SELECT id, username, password_hash, role, status, token_version, last_login_at, created_at, updated_at FROM app_user WHERE username = #{username} LIMIT 1")
    UserAccount findByUsername(@Param("username") String username);
}
