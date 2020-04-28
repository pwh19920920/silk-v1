package com.spark.bitrade.service;

import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 *  机器人服务接口
 * 备注：market模块无法支持feign
 *
 * @author young
 * @time 2019.09.06 18:01
 * @FeignClient("service-exchange-cyw")
 */
public interface ICywService {
    String uri_prefix = "http://service-exchange-cyw/ex_cyw/service/v1/cywOrder/";

    /**
     * 处理交易明细的买单
     *
     * @param trade 交易明细
     * @return
     */
    @Deprecated
    MessageRespResult<ExchangeOrder> tradeBuy(@RequestBody ExchangeTrade trade);


    /**
     * 处理交易明细的卖单
     *
     * @param trade 交易明细
     * @return
     */
    @Deprecated
    MessageRespResult<ExchangeOrder> tradeSell(@RequestBody ExchangeTrade trade);

    /**
     * 完成订单的处理
     *
     * @param memberId     用户ID
     * @param orderId      订单号
     * @param tradedAmount 成交数量
     * @param turnover     成交额
     * @return
     */
    @Deprecated
    MessageRespResult<ExchangeOrder> completedOrder(@RequestParam("memberId") Long memberId,
                                                    @RequestParam("orderId") String orderId,
                                                    @RequestParam("tradedAmount") BigDecimal tradedAmount,
                                                    @RequestParam("turnover") BigDecimal turnover);

    /**
     * 撤销订单的处理
     *
     * @param memberId     用户ID
     * @param orderId      订单号
     * @param tradedAmount 成交数量
     * @param turnover     成交额
     * @return
     */
    @Deprecated
    MessageRespResult<ExchangeOrder> canceledOrder(@RequestParam("memberId") Long memberId,
                                                   @RequestParam("orderId") String orderId,
                                                   @RequestParam("tradedAmount") BigDecimal tradedAmount,
                                                   @RequestParam("turnover") BigDecimal turnover);

    /**
     * 撤销订单的处理（无法提供成交额和成交量）
     *
     * @param memberId 用户ID
     * @param orderId  订单号
     * @return
     */
    @Deprecated
    MessageRespResult<ExchangeOrder> canceledOrder(@RequestParam("memberId") Long memberId,
                                                   @RequestParam("orderId") String orderId);

    /**
     * 重做
     *
     * @param id
     * @return
     */
    MessageResult redo(@RequestParam("id") Long id);
}
