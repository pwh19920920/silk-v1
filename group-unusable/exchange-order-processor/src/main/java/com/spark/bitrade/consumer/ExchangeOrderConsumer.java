package com.spark.bitrade.consumer;


import com.alibaba.fastjson.JSON;
import com.spark.bitrade.ApplicationEvent;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
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
public class ExchangeOrderConsumer {

    @Autowired
    private ProcessOrderService processOrderService;
    @Autowired
    private HandleOrderCompletedCacheAndConsumer handleOrderCompletedCacheAndConsumer;


    @KafkaListener(topics = "exchange-order-completed",group = "group-handle")
    public void handleOrderCompleted(ConsumerRecord<String,String> record){
        handleOrderCompletedCacheAndConsumer.put2HandleOrderCompletedQueue(record);
    }

    /**
     * 订单取消成功（在交易内存交易队列中）
     * @param record
     */
    @KafkaListener(topics = "exchange-order-cancel-success",group = "group-handle")
    public void handleOrderCanceled(ConsumerRecord<String,String> record){
        //long start = System.currentTimeMillis();
        processOrderService.asyncProcessOrderCancelSccess(record);
    }

    /**
     * 订单取消失败（不在内存交易队列中）
     * @param record
     */
    @KafkaListener(topics = "exchange-order-cancel-fail",group = "group-handle")
    public void handleOrderCanceled4Fail(ConsumerRecord<String,String> record){
        processOrderService.asyncProcessOrderCancelFail(record);
    }

}
