package com.spark.bitrade.consumer;


import com.alibaba.fastjson.JSON;
import com.spark.bitrade.cache.CoinCacheProcessorFactory;
import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.constant.KafkaTopicConstant;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.LatestTradeCacheService;
import com.spark.bitrade.service.PushTradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class MarketConsumer {
    @Autowired
    private PushTradeMessage pushTradeMessage;


    @Autowired
    private HandleTradePlateCacheAndConsumer handleTradePlateCacheAndConsumer;

    @Autowired
    private HandleOrderCompletedCacheAndConsumer handleOrderCompletedCacheAndConsumer;
    @Autowired
    private HandleOrderCanceledCacheAndConsumer handleOrderCanceledCacheAndConsumer;
    @Autowired
    private HandleTradeCacheAndConsumer handleTradeCacheAndConsumer;

//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TradeMessageDataManager tradeMessageDataManager;
    @Autowired
    private CoinCacheProcessorFactory coinCacheProcessorFactory;
    @Autowired
    private LatestTradeCacheService latestTradeCacheService;

    @Autowired
    private CoinExchangeRate coinExchangeRate;


//    @Value("${push.plate.size:7}")
//    private int push_plate_size; //盘口推送条数


    /**
     * 交易成功
     * @param record
     */
    @KafkaListener(topics = KafkaTopicConstant.MSG_EXCHANGE_ORDER_COMPLETED )
    public void handleOrderCompleted(ConsumerRecord<String,String> record){

        log.info("msg-exchange-order-completed,key={},value={}",record.key(), record.value());

        //ExchangeOrder order
        //kafkaTemplate.send("msg-exchange-order-completed", order.getSymbol(), JSON.toJSONString(order));
        handleOrderCompletedCacheAndConsumer.put2HandleOrderCompletedQueue(record);
    }

    /**
     * 订单取消成功
     * @param record
     */
    @KafkaListener(topics =KafkaTopicConstant.MSG_EXCHANGE_ORDER_CANCELED)
    public void handleOrderCanceled(ConsumerRecord<String,String> record){
        log.info("msg-exchange-order-canceled,key={},value={}",record.key(), record.value());
        handleOrderCanceledCacheAndConsumer.put2HandleOrderCanceledQueue(record);
        //ExchangeOrder order
        //kafkaTemplate.send("msg-exchange-order-canceled", order.getSymbol(), JSON.toJSONString(order));
    }

    /**
     * 消费 部分成交
     * @param record
     */
    @KafkaListener(topics = KafkaTopicConstant.MSG_EXCHANGE_TRADE)
    public void handleTrade(ConsumerRecord<String,String> record){
        log.info("msg-exchange-trade,key={},value={}",record.key(), record.value());
        handleTradeCacheAndConsumer.put2TradeQueue(record);

        //handleTradeCacheAndConsumer.put2HandleTradeQueue(record);
        //ExchangeOrder order
        //kafkaTemplate.send("msg-exchange-trade", order.getSymbol(), JSON.toJSONString(order));

    }



    /**
     * 消费 缩略行情
     * @param record
     */
    @KafkaListener(topics = KafkaTopicConstant.MSG_MARKET_COIN_THUMB)
    public void handleCoinThumb(ConsumerRecord<String,String> record){
        log.info("msg-market-coin-thumb,key={},value={}",record.key(), record.value());

        CoinThumb thumb = JSON.parseObject(record.value(), CoinThumb.class);
        if(null != thumb) {
            tradeMessageDataManager.pushCoinThumb(record.key(), thumb);

            //缓存
            coinCacheProcessorFactory.getProcessor(record.key()).setCoinThumb(thumb);
        }
        //handleTradeCacheAndConsumer.put2HandleTradeQueue(record);

        //CoinThumb thumb
        //kafkaTemplate.send("msg-market-coin-thumb", thumb.getSymbol(), JSON.toJSONString(thumb));
    }

    /**
     * 消费 k线数据
     * @param record
     */
    @KafkaListener(topics = KafkaTopicConstant.MSG_MARKET_KLINE )
    public void handleLine(ConsumerRecord<String,String> record){
        log.info("msg-market-kLine,key={},value={}",record.key(), record.value());
        KLine kLine = JSON.parseObject(record.value(), KLine.class);
        if(null != kLine) {
            tradeMessageDataManager.pushKLine(record.key(), kLine);

            //缓存
            coinCacheProcessorFactory.getProcessor(record.key()).setKline(kLine);
        }
        //handleTradeCacheAndConsumer.put2HandleTradeQueue(record);

        //KLine kLine
        //kafkaTemplate.send("msg-market-kLine", symbol, JSON.toJSONString(kLine));
    }


    /**
     * 消费交易盘口信息
     * @param record
     */
    @KafkaListener(topics = KafkaTopicConstant.MSG_TRADE_PLATE_FULL)
    public void handleTradePlate(ConsumerRecord<String,String> record){
        log.info("msg-trade-plate-full,key={},value={}",record.key(), record.value());
        // todo ????? 是否使用 handleTradePlateCacheAndConsumer类
        TradePlate plateFull = JSON.parseObject(record.value(), TradePlate.class);
        if(null != plateFull) {
            tradeMessageDataManager.pushTradePlate(plateFull);

            //缓存
            coinCacheProcessorFactory.getProcessor(record.key()).setTradePlate(plateFull);
        }

        //kafkaTemplate.send("msg-trade-plate-full", plateFull.getSymbol(), JSON.toJSONString(plateFull));
        ///handleTradePlateCacheAndConsumer.put2HandleTradePlateQueue(record);
    }


    /**
     * 最新实时成交
     * @param record
     */
    @KafkaListener(topics = KafkaTopicConstant.MSG_NEWEST_TRADE)
    public void handleNewestTrade(ConsumerRecord<String,String> record){
        log.info("msg-newest-trade,key={},value={}",record.key(), record.value());
        List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
        if(null != trades) {
            String symbol = record.key();
            trades.forEach(trade -> {
                tradeMessageDataManager.pushLatestTrade(symbol, trade);

                //缓存数据
                latestTradeCacheService.offer(symbol, trade);
            });
        }
        //kafkaTemplate.send("msg-newest-trade", symbol, JSON.toJSONString(trades));
    }

    /**
     * 最新实时成交
     * @param record
     */
    @KafkaListener(topics = KafkaTopicConstant.MSG_UPDATE_USD_PRICE)
    public void handleUpdateUsdPrice(ConsumerRecord<String,String> record){
        //log.debug("msg-update-usd-price,key={},value={}",record.key(), record.value());
        //List<ExchangeTrade> trades
        //kafkaTemplate.send("msg-newest-trade", symbol, JSON.toJSONString(trades));
        Double price = Double.valueOf(record.value());
        coinExchangeRate.setUsdCnyRate(BigDecimal.valueOf(price));
    }
}
