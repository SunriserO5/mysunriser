package com.mysunriser.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("account_email_change_token")
public class AccountEmailChangeToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String email;

    @TableField("token_hash")
    private String tokenHash;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("consumed_at")
    private LocalDateTime consumedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
