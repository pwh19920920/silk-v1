package com.spark.bitrade.config;

import com.spark.bitrade.constant.ExchangeOrderDirection;

/***
  * kafka缓存的key常量定义
  * @author yangch
  * @time 2018.08.27 10:59
  */
public class KafkaOffsetCacheKeyConstant {
    private static String exchangeTrade = "kafka:exchangeTrade:trade:";
    private static String exchangeOrderCompleted = "kafka:exchangeOrder:completed:";

    public static String getKeyExchangeTrade(String symbol, ExchangeOrderDirection direction) {
        if (ExchangeOrderDirection.BUY == direction) {
            return new StringBuilder(exchangeTrade).append(symbol).append(":buy").toString();
        } else {
            return new StringBuilder(exchangeTrade).append(symbol).append(":sell").toString();
        }
    }

    public static String getKeyExchangeOrderCompleted(String symbol) {
        return new StringBuilder(exchangeOrderCompleted).append(symbol).toString();
    }
}
