package com.spark.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 缩略行情
 *
 * @author yangch
 */
@Data
public class CoinThumb {
    /**
     * 交易对
     */
    private String symbol;

    /**
     * 开盘
     */
    private BigDecimal open = BigDecimal.ZERO;

    /**
     * 最高
     */
    private BigDecimal high = BigDecimal.ZERO;
    /**
     * 最低
     */
    private BigDecimal low = BigDecimal.ZERO;
    /**
     * 收盘
     */
    private BigDecimal close = BigDecimal.ZERO;

    /**
     * 涨幅量=收盘-开盘
     */
    private BigDecimal change = BigDecimal.ZERO.setScale(2);

    /**
     * 涨幅比例=涨幅量/开盘
     */
    private BigDecimal chg = BigDecimal.ZERO.setScale(2);

    /**
     * 24H成交量
     */
    private BigDecimal volume = BigDecimal.ZERO.setScale(2);

    /**
     * 成交额
     */
    private BigDecimal turnover = BigDecimal.ZERO;

    /**
     * 昨日收盘价
     */
    private BigDecimal lastDayClose = BigDecimal.ZERO;

    /**
     * 对usd汇率
     */
    private BigDecimal usdRate;

    /**
     * 基币对usd的汇率
     */
    private BigDecimal baseUsdRate;

    /**
     * 是否为扶持项目的交易对，true=是/false=不是
     */
    private Boolean support = false;
}
