package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "exchange_order_detail")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeOrderDetail {
    private String orderId;
    /**
     * 关联交易的订单号
     */
    private String refOrderId;
    /**
     * 交易对
     */
    private String symbol;

    /**
     * 成交价格
     */
    private BigDecimal price;
    /**
     * 基币USD汇率
     */
    private BigDecimal baseUsdRate;
    private BigDecimal amount;
    private BigDecimal turnover;
    private BigDecimal fee;
    /**
     * //交易手续费优惠数量
     */
    private BigDecimal feeDiscount;
    /**
     * 成交时间
     */
    private long time;
}
