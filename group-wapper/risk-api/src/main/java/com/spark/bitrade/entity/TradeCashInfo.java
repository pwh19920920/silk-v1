package com.spark.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 法币交易信息
 * @author Zhang Yanjun
 * @time 2018.12.27 14:14
 */
@Data
public class TradeCashInfo {
    /**
     * 交易动作
     */
    private int action;
    /**
     * 交易方向  0-买 1-卖
     */
    private int direction;
    /**
     * 币种
     */
    private String coin;
    /**
     * 数量
     */
    private BigDecimal amount;
    /**
     * 交易对象
     */
    private Member targetUser;
}
