package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.spark.bitrade.entity.ExchangeFastAccountDO;

/***
  * 闪兑总账户配置DAO
  * @author yangch
  * @time 2019.03.27 14:57
  */
@Mapper
public interface ExchangeFastAccountMapper extends SuperMapper<ExchangeFastAccountDO> {

    /**
     * 根据币种和应用ID获取闪兑总账户接口
     *
     * @param appId      必填，应用ID
     * @param coinSymbol 必填，闪兑币种，如BTC、LTC
     * @param baseSymbol 闪兑基币
     * @return
     */
    List<ExchangeFastAccountDO> findByAppIdAndCoinSymbol(
            @Param("appId") String appId, @Param("coinSymbol") String coinSymbol, @Param("baseSymbol") String baseSymbol);


}
