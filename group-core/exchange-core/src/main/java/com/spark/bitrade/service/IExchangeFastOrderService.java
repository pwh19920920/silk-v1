package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.ExchangeFastOrderDO;

import java.math.BigDecimal;

/**
 * 闪兑订单接口
 *
 * @author Zhang Yanjun
 * @time 2019.03.27 11:36
 */
public interface IExchangeFastOrderService extends IService<ExchangeFastOrderDO> {

    /**
     * 闪兑发起方接口
     *
     * @param memberId     会员ID
     * @param appId        应用ID
     * @param coinSymbol   闪兑币种名称
     * @param baseSymbol   闪兑基币名称
     * @param amount       闪兑数量
     * @param direction    兑换方向
     * @param currentPrice 汇率价，注意汇率和兑换方向有关
     * @return
     */
    ExchangeFastOrderDO exchangeInitiator(Long memberId, String appId,
                                          String coinSymbol, String baseSymbol, BigDecimal amount,
                                          ExchangeOrderDirection direction, BigDecimal currentPrice);


    /**
     * 闪兑接收方接口
     *
     * @param orderId 订单ID
     */
    void exchangeReceiver(Long orderId);
}
