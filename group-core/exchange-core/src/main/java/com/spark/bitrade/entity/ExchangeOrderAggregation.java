package com.spark.bitrade.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * 聚合订单最终的撮单结果
 * yangch
 * 2018-05-22
 */
@Data
public class ExchangeOrderAggregation {
    private String orderId;
    private BigDecimal amount; //数量
    private BigDecimal turnover; //交易额
}
