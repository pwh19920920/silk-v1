package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import com.spark.bitrade.entity.SilkDataDist;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SilkDataDistMapper extends SuperMapper<SilkDataDist> {

    SilkDataDist findByIdAndKey(@Param("dictId")String id,@Param("dictKey")String key);
    SilkDataDist findByKey(@Param("dictKey")String key);

    /**
     * 查询大活动下的多个小活动
     * @param id
     * @param key
     * @return
     */
    List<SilkDataDist> findListByIdAndKey(@Param("dictId")String id, @Param("dictKey")String key);
}
