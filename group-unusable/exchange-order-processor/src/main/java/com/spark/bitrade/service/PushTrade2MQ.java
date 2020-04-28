package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.KafkaTopicConstant;
import com.spark.bitrade.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/***
  * 推送交易信息
  * @author yangch
  * @time 2018.06.30 15:22 
  */

@Service
@Slf4j
public class PushTrade2MQ {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    /***
      * 异步推送成交订单信息
      * @author yangch
      * @time 2018.06.30 15:28 
     * @param order
     */
    @Async
    public void pushOrderCompleted(ExchangeOrder order){
        kafkaTemplate.send(KafkaTopicConstant.MSG_EXCHANGE_ORDER_COMPLETED, order.getSymbol(), JSON.toJSONString(order));

        //log.info("/topic/market/order-completed/{}/{}" ,order.getSymbol() , order.getMemberId());
        ///messagingTemplate.convertAndSend("/topic/market/order-completed/" + order.getSymbol() + "/" + order.getMemberId(), order);
    }

    /***
      * 异步推送撤单成功信息
      * @author yangch
      * @time 2018.06.30 15:28 
     * @param order
     */
    @Async
    public void pushOrderCanceled(ExchangeOrder order){
        kafkaTemplate.send(KafkaTopicConstant.MSG_EXCHANGE_ORDER_CANCELED, order.getSymbol(), JSON.toJSONString(order));
        //log.info("/topic/market/order-canceled/{}/{}" ,order.getSymbol() , order.getMemberId());
        ///messagingTemplate.convertAndSend("/topic/market/order-canceled/" + order.getSymbol() + "/" + order.getMemberId(), order);
    }
}
