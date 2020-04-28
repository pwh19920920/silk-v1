package com.spark.bitrade.mocker.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 表示一条交易推送消息
 *
 */
public class TradeMessage implements Serializable{
    private Long time;
    /**
     * 交易对名称，如btcusdt,ethusdt
     */
    private String symbol;//
    private List<Trade> tradeList;

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<Trade> getTradeList() {
        return tradeList;
    }

    public void setTradeList(List<Trade> tradeList) {
        this.tradeList = tradeList;
    }
}
