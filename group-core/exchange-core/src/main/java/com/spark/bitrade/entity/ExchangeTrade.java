package com.spark.bitrade.entity;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 撮合交易信息
 */
@Data
///@Document(collection = "exchange_order_trade") //del by yangch 时间： 2018.04.29 原因：不保存到
public class ExchangeTrade implements Serializable {
    private String symbol;
    /**
     * 成交价
     */
    private BigDecimal price;
    /**
     * 成交数量
     */
    private BigDecimal amount;
    /**
     * 买入成交额
     */
    private BigDecimal buyTurnover;
    /**
     * 卖出成交额
     */
    private BigDecimal sellTurnover;
    /**
     * 订单方向
     */
    private ExchangeOrderDirection direction;
    /**
     * 卖单用户ID
     */
    private Long buyMemberId;
    /**
     * 买单订单号
     */
    private String buyOrderId;
    /**
     * 卖单用户ID
     */
    private Long sellMemberId;
    /**
     * 卖单订单号
     */
    private String sellOrderId;
    /**
     * 未完成订单号
     */
    private String unfinishedOrderId;
    /**
     * 未完成订单的交易数量
     */
    private BigDecimal unfinishedTradedAmount;
    /**
     * 未完成订单的成交额
     */
    private BigDecimal unfinishedTradedTurnover;
    private Long time;

    /**
     * 基币USD汇率
     */
    private BigDecimal baseUsdRate;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
