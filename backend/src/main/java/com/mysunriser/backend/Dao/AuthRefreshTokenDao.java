package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.entity.AuthRefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthRefreshTokenDao extends BaseMapper<AuthRefreshToken> {
}
