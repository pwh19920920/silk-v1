package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * @author shenzucai
 * @time 2018.04.25 14:37
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinBase {

    /**
     * coinbase 地址
     */
    private String coinBase;

    /**
     * coinBase 余额
     */
    private BigDecimal balance;


    public String getCoinBase() {
        return coinBase;
    }

    public void setCoinBase(String coinBase) {
        this.coinBase = coinBase;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
