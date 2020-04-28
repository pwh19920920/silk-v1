package com.spark.bitrade.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author shenzucai
 * @time 2018.04.25 14:37
 */
public class CoinBase implements Serializable{

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
