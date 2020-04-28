package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.LockConstant;
import com.spark.bitrade.mq.CnytMarketRewardMessage;
import com.spark.bitrade.service.cnyt.LockMarketRewardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


/**
 * cnyt增值计划kafka监听器
 * @author tansitao
 * @time 2018/12/4 11:08 
 */
@Component
@Slf4j
public class Consumer {

    @Autowired
    private LockMarketRewardService lockMarketRewardService;
    @KafkaListener(topics = LockConstant.KAFKA_TX_CNYT_MESSAGE_HANDLER, group = "group-handle")
    public void handle(ConsumerRecord<String,String> record){
        /**
         * 1、获取kafka推过来的数据，将消息转为消息体
         * 2、异步调用消息处理
         * 2、处理完毕、继续向外抛出消息、（抛出返佣、更新推荐奖等消息）
         */
        CnytMarketRewardMessage memssage = JSON.parseObject(record.value(), CnytMarketRewardMessage.class);
        if(memssage != null){
            //处理消息
            lockMarketRewardService.dealCNYTMessage(memssage);
        }else {
            log.info("========================奖励明细推送的消息为空==============================");
        }

    }
}
