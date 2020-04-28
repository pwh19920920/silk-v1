package com.spark.bitrade.mocker.entity;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 撮合交易信息
 */
@Data
public class ExchangeTrade implements Serializable {
    private BigInteger id;
    private BigDecimal price;
    private BigDecimal amount;
    private ExchangeOrderDirection direction;
    private String buyOrderId;
    private String sellOrderId;
    private Long time;
    @Override
    public String toString() {
        return  JSON.toJSONString(this);
    }
}