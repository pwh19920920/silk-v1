package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.ClickBusinessConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 *  * 一键买币商家配置查询
 *  * @author tansitao
 *  * @time 2019/1/7 14:23 
 *  
 */
@Mapper
public interface ClickBusinessConfigMapper {

    //add by tansitao 时间： 2019/1/7 原因：获取所有一键买币卖币商家
    @Select("select * from click_business_config c where c.usable = 1")
    List<ClickBusinessConfig> findAll();

    @Select("select * from click_business_config c where c.usable = 1 and unit = #{coin} and app_id = #{appId}")
    List<ClickBusinessConfig> findByCoinAndAppId(@Param("coin") String coin, @Param("appId") String appId);
}
