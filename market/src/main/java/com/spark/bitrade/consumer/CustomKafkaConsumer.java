package com.spark.bitrade.consumer;

import com.spark.bitrade.config.CustomKafkaConsumerConfig;
import com.spark.bitrade.service.KafkaOffsetCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author tian.b
 * kafka offset 缓存消费
 * Created in 2018/8/18.
 */
@Service
@Slf4j
public class CustomKafkaConsumer {

    @Autowired
    private KafkaOffsetCacheService kafkaOffsetCacheService;

    @Autowired
    private CustomKafkaConsumerConfig customKafkaConsumerConfig;

    /**
     * 获取多个ConsumerRecords
     *
     * @param key       redis中设置的key
     * @param topicName kafka中的topic
     * @return
     */
    public List<ConsumerRecords<String, Object>> getConsumerRecords(String key, String topicName) {
        List<Object> offsetList = kafkaOffsetCacheService.opsForList(key, 0, -1);
        if (offsetList == null) {
            log.warn("key={},topicName={},offset is null", key, topicName);
            return null;
        }
        List<ConsumerRecords<String, Object>> consumerRecordsList = new ArrayList<>();
        KafkaConsumer<String, Object> consumer = new KafkaConsumer<String, Object>(customKafkaConsumerConfig.consumerConfigs());
        consumer.assign(Arrays.asList(new TopicPartition(topicName, 0)));
        //不改变当前offset
        consumer.seekToBeginning(Arrays.asList(new TopicPartition(topicName, 0)));
        for (Object offset : offsetList) {
            consumer.seek(new TopicPartition(topicName, 0), Long.valueOf(offset.toString()));
            ConsumerRecords<String, Object> records = consumer.poll(1000);
            consumerRecordsList.add(records);
        }
        return consumerRecordsList;
    }

    /**
     * 获取多个ConsumerRecords
     *
     * @param topicName kafka中的topic
     * @param keys      redis中设置的key
     * @return
     */
    public List<ConsumerRecord<String, String>> getConsumerRecord(String topicName, String... keys) {
        Set<Object> offsetList = new HashSet<>();
        for (String key : keys) {
            List list = kafkaOffsetCacheService.opsForList(key, 0, -1);
            if (null != list) {
                offsetList.addAll(list);
            }
        }
        if (offsetList.size() == 0) {
            log.warn("key={},topicName={},offset is null", keys, topicName);
            return null;
        }

        List<ConsumerRecord<String, String>> consumerRecordsList = new ArrayList<>();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(customKafkaConsumerConfig.consumerConfigs());
        consumer.assign(Arrays.asList(new TopicPartition(topicName, 0)));
        //不改变当前offset
        consumer.seekToBeginning(Arrays.asList(new TopicPartition(topicName, 0)));
        TopicPartition topicPartition = new TopicPartition(topicName, 0);
        for (Object offset : offsetList) {
            consumer.seek(topicPartition, Long.valueOf(offset.toString()));
            ConsumerRecords<String, String> record = consumer.poll(1000);
            if (null != record) {
                Iterator<ConsumerRecord<String, String>> iterator = record.iterator();
                if (iterator.hasNext()) {
                    //获取第一条记录
                    consumerRecordsList.add(iterator.next());
                }
            }
        }
        return consumerRecordsList;
    }

}
