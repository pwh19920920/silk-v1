package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.ExchangeFastAccountDO;

/**
 * @author Zhang Yanjun
 * @time 2019.03.27 11:06
 */
public interface IExchangeFastAccountService extends IService<ExchangeFastAccountDO> {

    /**
     * 根据币种和应用ID获取闪兑总账户接口
     *
     * @param appId      必填，应用ID
     * @param coinSymbol 必填，闪兑币种，如BTC、LTC
     * @param baseSymbol 闪兑基币
     * @return
     */
    ExchangeFastAccountDO findByAppIdAndCoinSymbol(String appId, String coinSymbol, String baseSymbol);
}
