package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.entity.PostMediaReference;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostMediaReferenceDao extends BaseMapper<PostMediaReference> {
    @Delete("DELETE FROM post_media_reference WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);

    @Insert("""
            INSERT IGNORE INTO post_media_reference(post_id, media_asset_id)
            VALUES (#{postId}, #{mediaAssetId})
            """)
    int insertReference(@Param("postId") Long postId, @Param("mediaAssetId") Long mediaAssetId);

    @Select("SELECT COUNT(*) FROM post_media_reference WHERE media_asset_id = #{mediaAssetId}")
    int countByMediaAssetId(@Param("mediaAssetId") Long mediaAssetId);

    @Select("""
            SELECT COUNT(*)
            FROM post_media_reference r
            JOIN post p ON p.id = r.post_id
            WHERE r.media_asset_id = #{mediaAssetId}
              AND LOWER(p.status) = 'published'
            """)
    int countPublishedReferences(@Param("mediaAssetId") Long mediaAssetId);
}
