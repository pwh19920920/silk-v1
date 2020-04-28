package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.AppRevisionDto;
import com.spark.bitrade.entity.AppRevision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author lingxing
 * @time 2018.07.14 08:52
 */
@Mapper
public interface AppRevisionMapper {
    @Select("SELECT * FROM app_revision a1 WHERE  a1.platform=#{platform} AND a1.version=#{version}")
    List<AppRevision> findByPlatformAndVersion(@Param("platform") int platform, @Param("version") String version);

    @Select("SELECT * FROM app_revision a1 WHERE  a1.id=#{id}")
    AppRevision findById(@Param("id") Long id);

    List<AppRevision> findByAppRevisionAll(@Param("appRevisionDto") AppRevisionDto appRevisionDto);

    @Select(" select * from app_revision where platform = #{platfrom} ORDER BY version desc LIMIT 0,1  ")
    AppRevision findRevisionByPlatFrom(@Param("platfrom") String platfrom);
}
