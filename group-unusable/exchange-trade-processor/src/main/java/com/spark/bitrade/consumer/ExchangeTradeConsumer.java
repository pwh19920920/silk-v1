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
public class ExchangeTradeConsumer {

    @Autowired
    private HandleTradeCacheAndConsumer handleTradeCacheAndConsumer;


    /**
     * 处理成交明细
     * @param record
     */
    @KafkaListener(topics = "exchange-trade",group = "group-handle")
    public void handleTrade(ConsumerRecord<String,String> record){
        handleTradeCacheAndConsumer.put2HandleTradeQueue(record);
    }

}
