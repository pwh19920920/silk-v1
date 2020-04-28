package com.spark.bitrade.constant;

/***
 * 
 * @author yangch
 * @time 2018.08.24 15:02
 */
public class KafkaTopicConstant {
    //管理指定交易对的撮合器
    public final static String exchangeTraderManager = "exchange-trader-manager";

    //管理指定交易对的处理器
    public final static String exchangeProcessorManager = "exchange-processor-manager";

    //消息类型
    public final static String MSG_EXCHANGE_ORDER_COMPLETED = "msg-exchange-order-completed"; //成交订单推送消息
    public final static String MSG_EXCHANGE_ORDER_CANCELED = "msg-exchange-order-canceled";  //撤单成功推送消息
    public final static String MSG_EXCHANGE_TRADE = "msg-exchange-trade";  //部分成交推送消息
    public final static String MSG_MARKET_COIN_THUMB = "msg-market-coin-thumb"; //行情推送消息
    public final static String MSG_MARKET_KLINE = "msg-market-kLine"; //k线推送消息
    public final static String MSG_TRADE_PLATE_FULL = "msg-trade-plate-full"; //盘口推送消息
    public final static String MSG_NEWEST_TRADE = "msg-newest-trade"; //实时成交推送消息
    public final static String MSG_UPDATE_USD_PRICE = "msg-update-usd-price"; //USD实时推送消息
}
