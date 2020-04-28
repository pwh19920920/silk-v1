package com.spark.bitrade.controller.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *  盘口累计数量
 *
 * @author young
 * @time 2019.12.09 16:59
 */
@Data
public class TradePlateTotalVo {
    /**
     * 卖盘总数量
     */
    private BigDecimal askTotal = BigDecimal.ZERO;

    /**
     * 买盘总数量
     */
    private BigDecimal bidTotal = BigDecimal.ZERO;
}
