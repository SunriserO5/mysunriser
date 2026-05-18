package com.mysunriser.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("online_tool")
public class OnlineTool {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String slug;

    private String title;

    private String summary;

    private String status;

    @TableField("entry_type")
    private String entryType;

    @TableField("route_path")
    private String routePath;

    @TableField("external_url")
    private String externalUrl;

    @TableField("access_level")
    private String accessLevel;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
