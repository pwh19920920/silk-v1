package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.Announcement;
import org.apache.ibatis.annotations.Param;

/**
 * @author fumy
 * @time 2018.11.19 20:36
 */
public interface AnnouncementMapper {

    int isExistGlobalTop(@Param("isTop") int isTop);

    Announcement queryByGlobalTop(@Param("platform") int platform, @Param("languageCode") String languageCode);

}
