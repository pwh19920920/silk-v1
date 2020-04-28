package com.spark.bitrade.entity;

import com.spark.bitrade.constant.BooleanEnum;

import java.math.BigDecimal;

/**
 * <p>红包实体</p>
 * @author octopus
 */
public class RedPacket {

    /**
     * 红包币种
     */
    private String symbol;

    /**
     * 红包面额
     */
    private BigDecimal amount;

    /**
     * 是否最大面额红包
     */
    private BooleanEnum isMax;

    public BooleanEnum getIsMax() {
        return isMax;
    }

    public void setIsMax(BooleanEnum isMax) {
        this.isMax = isMax;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
