package com.spark.bitrade.mapper.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.spark.bitrade.entity.StartupImgConfig;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StartupImgConfigMapper {

    int insert(@Param("pojo") StartupImgConfig pojo);

    int insertList(@Param("pojos") List< StartupImgConfig> pojo);

    List<StartupImgConfig> select(@Param("pojo") StartupImgConfig pojo);

    int update(@Param("pojo") StartupImgConfig pojo);

    /**
     * 查找所有数据
     */
    @Select("SELECT * FROM startup_img_config s")
    List<StartupImgConfig> findAll();

}
