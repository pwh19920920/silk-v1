package com.spark.bitrade.consumer;


import com.alibaba.fastjson.JSON;
import com.spark.bitrade.ApplicationEvent;
import com.spark.bitrade.component.CoinProcessorManager;
import com.spark.bitrade.consumer.handle.HandleOrderCompletedCacheAndConsumer;
import com.spark.bitrade.consumer.handle.HandleTradeCacheAndConsumer;
import com.spark.bitrade.consumer.handle.HandleTradePlateCacheAndConsumer;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
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
    private HandleOrderCompletedCacheAndConsumer handleOrderCompletedCacheAndConsumer;

    @Autowired
    private CoinProcessorManager coinProcessorManager;
    @Autowired
    private ApplicationEvent applicationEvent;


    /**
     * 分发成交明细集合
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-trade", group = "group-handle")
    public void handleTrade(ConsumerRecord<String, String> record) {
        handleTradeCacheAndConsumer.put2HandleTradeQueue(record);
    }


    /**
     * 分发已成交订单集合
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-order-completed", group = "group-handle")
    public void handleOrderCompleted(ConsumerRecord<String, String> record) {
        handleOrderCompletedCacheAndConsumer.put2HandleOrderCompletedQueue(record);
    }

    /**
     * 处理模拟交易
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-trade-mocker", group = "group-handle")
    public void handleMockerTrade(ConsumerRecord<String, String> record) {
        logger.info("topic={},key={},value={}", record.topic(), record.key(), record.value());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 消费交易盘口信息
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-trade-plate", group = "group-handle")
    public void handleTradePlate(ConsumerRecord<String, String> record) {
        handleTradePlateCacheAndConsumer.put2HandleTradePlateQueue(record);
    }

//    /**
//     * 订单取消成功（在交易内存交易队列中）
//     *
//     * @param record
//     */
////    @KafkaListener(topics = "exchange-order-cancel-success", group = "group-handle")
//    public void handleOrderCanceled(ConsumerRecord<String, String> record) {
//        processOrderService.asyncProcessOrderCancelSccess(record);
//    }
//
//    /**
//     * 订单取消失败（不在内存交易队列中）
//     *
//     * @param record
//     */
////    @KafkaListener(topics = "exchange-order-cancel-fail", group = "group-handle")
//    public void handleOrderCanceled4Fail(ConsumerRecord<String, String> record) {
//        processOrderService.asyncProcessOrderCancelFail(record);
//    }


    /**
     * 管理指定交易对的处理器
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-processor-manager", group = "group-handle")
    public void onCoinProcessorManager(ConsumerRecord<String, String> record) {
        log.info("onCoinProcessorManager:topic={}, key={}", record.topic(), record.key());
        String symbol = record.key();
        ExchangeCoin exchangeCoin = JSON.parseObject(record.value(), ExchangeCoin.class);
        if (exchangeCoin == null) {
            return;
        }

        if (exchangeCoin.getEnable() == 1) {
            // 启用
            coinProcessorManager.onlineCoinProcessor(coinProcessorFactory, exchangeCoin);
            applicationEvent.resumeCoinTrader(exchangeCoin);
        } else if (exchangeCoin.getEnable() == 2) {
            // 禁用
            coinProcessorManager.offlineCoinProcessor(coinProcessorFactory, exchangeCoin);
        } else {
            log.warn("onCoinTraderReset:命令错误!! topic={},key={}", record.topic(), record.key());
        }
    }
}
