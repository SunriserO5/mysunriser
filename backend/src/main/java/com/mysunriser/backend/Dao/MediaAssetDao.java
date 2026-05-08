package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.entity.MediaAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MediaAssetDao extends BaseMapper<MediaAsset> {
    @Results({
            @Result(column = "asset_type", property = "assetType"),
            @Result(column = "original_filename", property = "originalFilename"),
            @Result(column = "storage_key", property = "storageKey"),
            @Result(column = "mime_type", property = "mimeType"),
            @Result(column = "size_bytes", property = "sizeBytes"),
            @Result(column = "access_level", property = "accessLevel"),
            @Result(column = "uploaded_by", property = "uploadedBy"),
            @Result(column = "created_at", property = "createdAt")
    })
    @Select("""
            SELECT id, asset_type, original_filename, storage_key, mime_type, size_bytes,
                   access_level, uploaded_by, created_at
            FROM media_asset
            ORDER BY created_at DESC, id DESC
            """)
    Page<MediaAsset> selectAdminPage(Page<?> page);
}
