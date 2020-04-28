package com.spark.bitrade.mocker.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 撮合交易信息
 */
public class Trade implements Serializable{
    private BigDecimal price;
    private BigDecimal amount;
    private String direction;
    private Long time;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
