package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.entity.PasswordResetToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PasswordResetTokenDao extends BaseMapper<PasswordResetToken> {
}
