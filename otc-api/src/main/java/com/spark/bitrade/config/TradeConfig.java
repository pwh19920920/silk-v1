package com.spark.bitrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
  * 交易控制配置类
  * @author tansitao
  * @time 2018/5/17 9:15 
  */
@Configuration
@ConfigurationProperties("trade")
public class TradeConfig {
    private int isOpenLimit;
    private int orderNum;
    private int orderCancleNum;

    public int getOrderCancleNum() {
        return orderCancleNum;
    }

    public void setOrderCancleNum(int orderCancleNum) {
        this.orderCancleNum = orderCancleNum;
    }

    public int getIsOpenLimit() {
        return isOpenLimit;
    }

    public void setIsOpenLimit(int isOpenLimit) {
        this.isOpenLimit = isOpenLimit;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }
}
