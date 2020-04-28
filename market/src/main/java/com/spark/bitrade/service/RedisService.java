package com.spark.bitrade.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author tian.b
 * redis 操作api
 * Created in 2018/8/18.
 */
@Service
public class RedisService {

    @Autowired
    @Resource(name = "redisTemplate01")
    private RedisTemplate redisTemplate;

    /**
     * 默认过期时长，单位：秒
     */
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24;

    /**
     * 设置key的生命周期
     *
     * @param key      key
     * @param time     过期时间
     * @param timeUnit 时间单位
     */
    public void expireKey(String key, long time, TimeUnit timeUnit) {
        redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 查询key的生命周期
     *
     * @param key      key
     * @param timeUnit 时间单位
     * @return
     */
    public long getKeyExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 设置key为永久有效
     *
     * @param key
     */
    public void persistKey(String key) {
        redisTemplate.persist(key);
    }

    /**
     * 将一个或者多个值推入列表的右端
     *
     * @param key   key
     * @param value value
     * @return
     */
    public Long rightPush(String key, Object value) {
        Long result = redisTemplate.opsForList().rightPush(key, value);
        return result;
    }

    /**
     * 将一个或者多个值推入列表的左端
     *
     * @param key   key
     * @param value value
     * @return
     */
    public Long leftPush(String key, Object value) {
        Long result = redisTemplate.opsForList().leftPush(key, value);
        return result;
    }

    /**
     * 获取列表
     *
     * @param key key
     * @return
     */
    public List<Object> opsForList(String key, long l1, long l2) {
        return redisTemplate.opsForList().range(key, l1, l2);
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 根据参数 count 的值，移除列表中与参数 value 相等的元素
     * count 的值可以是以下几种：
     * count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count
     * count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值
     * count = 0 : 移除表中所有与 value 相等的值
     *
     * @param key   key
     * @param count 检索位置
     * @param value 被删除的元素
     * @return
     */
    public Long remove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }


}
