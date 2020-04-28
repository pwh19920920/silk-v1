package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.ExchangeFastCoinDO;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.03.27 10:37
 */
public interface IExchangeFastCoinService extends IService<ExchangeFastCoinDO> {

    int save(ExchangeFastCoinDO fastCoinDO);

    /**
     * 根据币种和应用ID获取闪兑币种配置接口
     *
     * @param appId      必填，应用ID
     * @param coinSymbol 必填，闪兑币种，如BTC、LTC
     * @return
     */
    ExchangeFastCoinDO findByAppIdAndCoinSymbol(String appId, String coinSymbol);

    /**
     * 根据币种和应用ID获取闪兑币种配置接口
     *
     * @param appId      必填，应用ID
     * @param coinSymbol 必填，闪兑币种，如BTC、LTC
     * @param baseSymbol 闪兑基币
     * @return
     */
    ExchangeFastCoinDO findByAppIdAndCoinSymbol(String appId, String coinSymbol, String baseSymbol);

    /**
     * 闪兑支持币种的列表接口
     *
     * @param appId      必填，应用ID
     * @param baseSymbol 闪兑基币
     * @return
     */
    List<ExchangeFastCoinDO> list4CoinSymbol(String appId, String baseSymbol);

    /**
     * 闪兑基币币种的列表接口
     *
     * @param appId 必填，应用ID
     * @return
     */
    List<String> list4BaseSymbol(String appId);

    /**
     * 从配置中获取有效的基币汇率币种
     *
     * @param exchangeFastCoin 币种配置
     * @return 有效汇率的币种
     */
    String getRateValidBaseSymbol(ExchangeFastCoinDO exchangeFastCoin);

    /**
     * 从配置中获取有效的闪兑汇率币种
     *
     * @param exchangeFastCoin 币种配置
     * @return 有效汇率的币种
     */
    String getRateValidCoinSymbol(ExchangeFastCoinDO exchangeFastCoin);
}
