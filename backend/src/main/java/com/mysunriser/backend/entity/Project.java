package com.mysunriser.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String slug;

    private String title;

    private String summary;

    private String status;

    @TableField("repo_owner")
    private String repoOwner;

    @TableField("repo_name")
    private String repoName;

    @TableField("repo_url")
    private String repoUrl;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("readme_markdown")
    private String readmeMarkdown;

    @TableField("readme_etag")
    private String readmeEtag;

    @TableField("readme_cached_at")
    private LocalDateTime readmeCachedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
