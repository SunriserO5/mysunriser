package com.mysunriser.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("auth_refresh_token")
public class AuthRefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("token_hash")
    private String tokenHash;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("revoked_at")
    private LocalDateTime revokedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("created_ip")
    private String createdIp;

    @TableField("created_user_agent")
    private String createdUserAgent;
}
