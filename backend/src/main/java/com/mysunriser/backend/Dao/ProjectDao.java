package com.mysunriser.backend.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mysunriser.backend.entity.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectDao extends BaseMapper<Project> {
}
