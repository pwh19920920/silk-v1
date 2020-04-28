package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.spark.bitrade.entity.ExchangeFastCoinDO;

/***
  * 闪兑币种配置DAO
  * @author yangch
  * @time 2019.03.27 13:48
  */
@Mapper
public interface ExchangeFastCoinMapper extends SuperMapper<ExchangeFastCoinDO> {

    /**
     * 根据币种和应用ID获取闪兑币种配置接口
     *
     * @param appId      必填，应用ID
     * @param coinSymbol 必填，闪兑币种，如BTC、LTC
     * @return
     */
    ExchangeFastCoinDO findByAppIdAndCoinSymbol(@Param("appId") String appId,
                                                @Param("coinSymbol") String coinSymbol,
                                                @Param("baseSymbol") String baseSymbol);

    /**
     * 闪兑支持币种的列表接口
     *
     * @param appId      必填，应用ID
     * @param baseSymbol 基币
     * @return
     */
    List<ExchangeFastCoinDO> list4CoinSymbol(@Param("appId") String appId, @Param("baseSymbol") String baseSymbol);

    /**
     * 闪兑基币币种的列表接口
     *
     * @param appId 必填，应用ID
     * @return
     */
    List<String> list4BaseSymbol(@Param("appId") String appId);
}
