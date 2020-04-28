package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.Updating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lingxing
 * @time 2018.07.20 15:01
 */
@Mapper
public interface UpdatingMapper {
    @Select("select * from updating")
    public List<Updating>getAll();
    @Select("select * from updating where id=#{id}")
    public Updating findById(@Param("id")Integer id);

}
