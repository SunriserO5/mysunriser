package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.dto.PostResponse;
import com.mysunriser.backend.entity.post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostDao extends BaseMapper<post>{

    @Select("SELECT * from post WHERE slug=#{slug}")
    post getBySlug(@Param("slug") String slug);

    @Select("SELECT * from post WHERE title=#{title}")
    PostResponse getByTitle(@Param("title") String title);
}

