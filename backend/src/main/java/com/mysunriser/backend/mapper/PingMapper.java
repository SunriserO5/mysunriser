package com.mysunriser.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface PingMapper {
    @Select("SELECT 1")
    int ping();
}
