package com.spark.bitrade.service;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 *  @author tian.b
 *  kafka offset 缓存 redis
 * Created in 2018/8/18.
 */
@Service
public class KafkaOffsetCacheService {

    @Autowired
    private RedisService redisService;

    /**
     * 设置key的生命周期
     * @param key
     *             key
     * @param time
     *             过期时间
     * @param timeUnit
     *              时间单位
     */
    public void expireKey(String key, long time, TimeUnit timeUnit) {
        redisService.expireKey(key, time, timeUnit);
    }


    /**
     * 将offset推入缓存列表右端
     * @param key
     *             key
     * @param record
     *             kafka consumer
     * @return
     */
    public Long  rightPushList(String  key, ConsumerRecord<String,String> record){
        return  redisService.rightPush(key, record.offset());
    }

    /**
     * 将offset推入缓存列表左端
     * @param key
     *             key
     * @param record
     *             kafka consumer
     * @return
     */
    public Long  leftPushList(String  key, ConsumerRecord<String,String> record){
        return  redisService.leftPush(key, record.offset());
    }

    /**
     * 获取列表
     * @param key
     *             key
     * @return
     */
    public List<Object>  opsForList(String key, long l1, long l2){
        return redisService.opsForList(key,l1,l2);
    }

    /**
     * 删除对应的value
     * @param key
     */
    public void remove(final String key) {
        redisService.remove(key);
    }

    /**
     * 从count位置开始检索，删除列表中和value相等的元素
     * 如：count=0，表示从列表头部开始检索，删除所有和value相等的元素
     * @param key
     *             key
     * @param count
     *              检索位置
     * @param value
     *             被删除的元素
     * @return
     */
    public Long removeListItem(String key, long count, Object value){
        return redisService.remove(key,count,value);
    }

}
