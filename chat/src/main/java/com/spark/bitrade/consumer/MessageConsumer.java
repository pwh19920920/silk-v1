package com.spark.bitrade.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
  * 推送消息
  * @author tansitao
  * @time 2018/9/18 20:07 
  */
@Component
public class MessageConsumer {
    private Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    /**
     * 处理全局消息
     * @param record
     */
    @KafkaListener(topics = "msg-promote-guess-handler",group = "group-handle")
    public void handleMessage(ConsumerRecord<String,String> record){
        logger.info("topic={},key={},value={}",record.topic(),record.key(),record.value());
        if(StringUtils.isEmpty(record.value())){
            return ;
        }
        JSONObject json = JSON.parseObject(record.value());
        if(json == null){
            return ;
        }
        String message = record.value();
        messagingTemplate.convertAndSend("/network/message", message);
    }

}
