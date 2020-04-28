package com.spark.bitrade.consumer;


import com.alibaba.fastjson.JSON;
import com.spark.bitrade.ApplicationEvent;
import com.spark.bitrade.component.CoinProcessorManager;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.PushTradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class ExchangeTradeConsumer {
    private Logger logger = LoggerFactory.getLogger(ExchangeTradeConsumer.class);
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;


    @Autowired
    private HandleTradeCacheAndConsumer handleTradeCacheAndConsumer;

    @Autowired
    private HandleTradePlateCacheAndConsumer handleTradePlateCacheAndConsumer;

    @Autowired
    private TradeMessageDataManager tradeMessageDataManager;

    @Autowired
    private CoinProcessorManager coinProcessorManager;
    @Autowired
    private ApplicationEvent applicationEvent;

//    @Value("${push.plate.size:7}")
//    private int push_plate_size; //盘口推送条数

    /**
     * 处理成交明细
     * @param record
     */
    @KafkaListener(topics = "exchange-trade-market",group = "group-handle")
    public void handleTrade(ConsumerRecord<String,String> record){
        handleTradeCacheAndConsumer.put2HandleTradeQueue(record);
    }

    /**
     * 处理模拟交易
     * @param record
     */
    @KafkaListener(topics = "exchange-trade-mocker",group = "group-handle")
    public void handleMockerTrade(ConsumerRecord<String,String> record){
        //logger.info("==========================exchange-trade-mocker=============================================");
        log.debug("topic={},key={},value={}",record.topic(),record.key(),record.value());
        try {
            List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
            String symbol = record.key();
            //处理行情
            CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
            if (coinProcessor != null) {
                logger.debug("unit = {} ,coinProcessor exist ", symbol);
                coinProcessor.process(trades);
            } else {
                logger.warn("unit = {} ,coinProcessor null ", symbol);
            }

            //edit by yangch 时间： 2018.06.30 原因：修改为异步推送实时成交信息
            //推送实时成交
            //messagingTemplate.convertAndSend("/topic/market/trade/" + symbol, trades);
            ///pushTradeMessage.pushNewestTrade4Socket(symbol, trades);
            // by tian.b 2018.8.15 推送信息密集优化
            tradeMessageDataManager.pushLatestTrade4Socket(symbol,trades);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 消费交易盘口信息
     * @param record
     */
    @KafkaListener(topics = "exchange-trade-plate",group = "group-handle")
    public void handleTradePlate(ConsumerRecord<String,String> record){
        handleTradePlateCacheAndConsumer.put2HandleTradePlateQueue(record);
    }

    //管理指定交易对的处理器
    @KafkaListener(topics = "exchange-processor-manager",group = "group-handle")
    public void onCoinProcessorManager(ConsumerRecord<String,String> record) {
        log.info("onCoinProcessorManager:topic={},key={}", record.topic(), record.key());
        String symbol = record.key();
        ExchangeCoin exchangeCoin = JSON.parseObject(record.value(), ExchangeCoin.class);
        if (exchangeCoin == null) {
            return;
        }

        if(exchangeCoin.getEnable() == 1) {
            //启用
            coinProcessorManager.onlineCoinProcessor(coinProcessorFactory, exchangeCoin);
            applicationEvent.recoverCoinTraderData(exchangeCoin);
        } else if(exchangeCoin.getEnable() == 2) {
            //禁用
            coinProcessorManager.offlineCoinProcessor(coinProcessorFactory, exchangeCoin);
        } else {
            log.warn("onCoinTraderReset:命令错误!! topic={},key={}", record.topic(), record.key());
        }
    }
}
