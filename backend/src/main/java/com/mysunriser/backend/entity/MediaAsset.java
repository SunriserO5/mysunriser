package com.mysunriser.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("media_asset")
public class MediaAsset {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_type")
    private String assetType;

    @TableField("original_filename")
    private String originalFilename;

    @TableField("storage_key")
    private String storageKey;

    @TableField("mime_type")
    private String mimeType;

    @TableField("size_bytes")
    private Long sizeBytes;

    @TableField("access_level")
    private String accessLevel;

    @TableField("uploaded_by")
    private String uploadedBy;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
