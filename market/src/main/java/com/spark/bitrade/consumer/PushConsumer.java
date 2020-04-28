package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.service.PushTradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 *  消费订单的推送任务
 *
 * @author young
 * @time 2019.10.23 16:56
 */
@Component
@Slf4j
public class PushConsumer {

    @Autowired
    private PushTradeMessage pushTradeMessage;

    /**
     * 推送部分成交订单消息
     *
     * @param record
     */
    @KafkaListener(topics = "push-order-trade", group = "group-handle")
    public void handlePushOrderTrade(ConsumerRecord<String, String> record) {
        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
        pushTradeMessage.pushOrderTrade4Socket(order);
        pushTradeMessage.pushOrderTrade4Netty(order);
    }

    /**
     * 推送已成交订单消息
     *
     * @param record
     */
    @KafkaListener(topics = "push-order-completed", group = "group-handle")
    public void handlePushOrderCompleted(ConsumerRecord<String, String> record) {
        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
        pushTradeMessage.pushOrderCanceled4Socket(order);
        pushTradeMessage.pushOrderCanceled4Netty(order);
    }

    /**
     * 推送已撤单订单消息
     *
     * @param record
     */
    @KafkaListener(topics = "push-order-canceled", group = "group-handle")
    public void handlePushOrderCanceled(ConsumerRecord<String, String> record) {
        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);

        pushTradeMessage.pushOrderCanceled4Socket(order);
        pushTradeMessage.pushOrderCanceled4Netty(order);
    }

    /**
     * 推送钱包同步成功的消息
     *
     * @param record
     */
    @KafkaListener(topics = "push_ex_wallet_sync_succeed", group = "group-handle")
    public void handlePushWalletSyncSucceed(ConsumerRecord<String, String> record) {
        log.info("推送钱包同步成功的消息。coinsymbol={}, memberId={}", record.key(), record.value());
        pushTradeMessage.handlePushWalletSyncSucceed(record.value(), record.key());
    }
}
