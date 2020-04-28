package com.spark.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradePlateItem {
    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private BigDecimal amount;

    /**
     * 累计数量
     */
    private BigDecimal totalAmount;
}
