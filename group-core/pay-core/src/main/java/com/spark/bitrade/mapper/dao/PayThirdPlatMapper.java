package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.ThirdPlatform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author fumy
 * @time 2018.10.23 16:23
 */
@Mapper
public interface PayThirdPlatMapper {

    List<ThirdPlatform> getThirdPlayList(@Param("platName")String platName);

    ThirdPlatform getByKey(@Param("applyKey") String applyKey);
}
