package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.entity.PageItems;
import com.mysunriser.backend.entity.post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostDao extends BaseMapper<post>{

    @Select("SELECT * from post WHERE slug=#{slug}")
    post getBySlug(@Param("slug") String slug);

    @Select("SELECT * from post WHERE title=#{title}")
    post getByTitle(@Param("title") String title);

    @Select("SELECT slug, title, summary, status, published_at AS publishTime FROM post ORDER BY published_at DESC")
    // IPage<PageItems> selectPageItems(Page<?> page);
    @org.apache.ibatis.annotations.ResultType(com.mysunriser.backend.entity.PageItems.class)
    Page<PageItems> selectPageItems(Page<?> page);
}

