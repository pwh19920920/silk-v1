package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.SysAdvertise;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author fumy
 * @time 2018.11.19 13:55
 */
public interface SysAdvertiseMapper {

    int isExistAppActNormalAd(@Param("type")int adType,@Param("location")int adLocation,@Param("status")int status);

    List<SysAdvertise> queryNormalAdByTypeAndLocation(@Param("type")int type,@Param("location")int location,@Param("languageCode")String languageCode);
}
