package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.MerchantBuyConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.07.12 10:59  
 */
@Mapper
public interface MerchantBuyConfigMapper {

    @Select("SELECT * FROM merchant_buy_config WHERE unit=#{coinUnit} AND app_id=#{appId} AND usable=1")
    List<MerchantBuyConfig> findByCoinUnit(@Param("coinUnit")String coinUnit,@Param("appId")String appId);


}
