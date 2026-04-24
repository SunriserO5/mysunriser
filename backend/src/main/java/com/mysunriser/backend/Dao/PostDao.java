package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.entity.PageItems;
import com.mysunriser.backend.entity.post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostDao extends BaseMapper<post>{

    @Results({
            @Result(column = "published_at", property = "published_at")
    })
    @Select("SELECT id, slug, title, content, status, COALESCE(published_at, created_at) AS published_at FROM post WHERE slug=#{slug}")
    post getBySlug(@Param("slug") String slug);

    @Select("SELECT * from post WHERE title=#{title}")
    post getByTitle(@Param("title") String title);

    @Select("SELECT slug, title, summary, status, published_at AS publishAt FROM post ORDER BY published_at DESC")
    // IPage<PageItems> selectPageItems(Page<?> page);
    @org.apache.ibatis.annotations.ResultType(com.mysunriser.backend.entity.PageItems.class)
    Page<PageItems> selectPageItems(Page<?> page);
}
