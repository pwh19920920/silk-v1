package com.spark.bitrade.messager.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author ww
 * @time 2019.09.18 11:37
 */
public interface IKafkaConsumerService {
    public boolean processKafkaConsumerMessage(ConsumerRecord<String, String> record, Acknowledgment ack);
    //public void SendKafkaMessage(Object);
}
