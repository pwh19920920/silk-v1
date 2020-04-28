package com.spark.bitrade.entity;


import lombok.Data;

import java.math.BigDecimal;

/**
 * K线
 */
@Data
public class KLine {
    public KLine() {
    }

    public KLine(String period) {
        this.period = period;
    }

    /**
     * 交易对符号。eg：SLB/USDT
     */
    private String symbol;
    /**
     * k线周期，[1,5,10,15,30]min、[1]hour、[1]week、[1]day、[1]month
     */
    private String period;
    /**
     * K线时间
     */
    private long time;

    /**
     * 开盘价
     */
    private BigDecimal openPrice = BigDecimal.ZERO;
    /**
     * 最高价
     */
    private BigDecimal highestPrice = BigDecimal.ZERO;
    /**
     * 最低价
     */
    private BigDecimal lowestPrice = BigDecimal.ZERO;
    /**
     * 收盘价
     */
    private BigDecimal closePrice = BigDecimal.ZERO;

    /**
     * 成交笔数
     */
    private int count = 0;
    /**
     * 成交量
     */
    private BigDecimal volume = BigDecimal.ZERO;
    /**
     * 成交额
     */
    private BigDecimal turnover = BigDecimal.ZERO;
}
