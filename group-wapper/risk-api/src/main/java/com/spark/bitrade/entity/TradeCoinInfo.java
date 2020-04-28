package com.spark.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 币币交易信息
 * @author Zhang Yanjun
 * @time 2018.12.27 10:48
 */
@Data
public class TradeCoinInfo {
    /**
     * 交易方向  0-买 1-卖
     */
    private int direction;
    /**
     * 交易币种
     */
    private String sourceCoin;
    /**
     * 结算币种
     */
    private String targetCoin;
    /**
     * 交易数量
     */
    private BigDecimal sourceAmount;
    /**
     * 结算数量
     */
    private BigDecimal targetAmount;
}
