package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.entity.AccountEmailChangeToken;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountEmailChangeTokenDao extends BaseMapper<AccountEmailChangeToken> {

    @Delete("DELETE FROM account_email_change_token WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}
