package com.mysunriser.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pending_registration")
public class PendingRegistration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("token_hash")
    private String tokenHash;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("consumed_at")
    private LocalDateTime consumedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
