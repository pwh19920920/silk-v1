package com.spark.bitrade.service;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author tian.b
 * redis 操作api
 * Created in 2018/8/18.
 */
@Service
public class RedisService {

    @Autowired
    //edit by tansitao 时间： 2018/10/26 原因：注释掉通过Resource注入，原因是用该方式无法处理缓存
//    @Resource(name = "redisTemplate01")
    private RedisTemplate redisTemplate;

    /**
     * 默认过期时长，单位：秒
     */
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24;

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
        redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 查询key的生命周期
     * @param key
     *             key
     * @param timeUnit
     *             时间单位
     * @return
     */
    public long getKeyExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 设置key为永久有效
     * @param key
     */
    public void persistKey(String key) {
        redisTemplate.persist(key);
    }

    /**
     * 将一个或者多个值推入列表的右端
     * @param key
     *             key
     * @param value
     *             value
     * @return
     */
    public Long  rightPush(String  key, Object value){
        Long result = redisTemplate.opsForList().rightPush(key, value);
        return  result;
    }

    /**
     * 将一个或者多个值推入列表的左端
     * @param key
     *             key
     * @param value
     *             value
     * @return
     */
    public Long  leftPush(String  key, Object value){
        Long result = redisTemplate.opsForList().leftPush(key, value);
        return  result;
    }

    /**
     * 获取列表
     * @param key
     *             key
     * @return
     */
    public List<Object>  opsForList(String key, long l1, long l2){
        return redisTemplate.opsForList().range(key,l1, l2);
    }

    /**
     * 删除对应的value
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }
    /**
     * 判断缓存中是否有对应的value
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
     * @param key
     *             key
     * @param count
     *              检索位置
     * @param value
     *             被删除的元素
     * @return
     */
    public Long remove(String key,long count, Object value){
        return redisTemplate.opsForList().remove(key,count,value);
    }


    /**
     * 模糊删除对应的value
     * @author tansitao
     * @time 2018/10/24 17:57 
     */
    public void removeLike(final String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        if (!StringUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 设置数据到redis，并设置过期时间
     * @author tansitao
     * @time 2018/10/26 16:51 
     */
    public void expireSet(String key, Object value, int time) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value, time, TimeUnit.MINUTES);
    }

    /**
     * 从redis中获取数据
     * @author tansitao
     * @time 2018/10/26 17:06 
     */
    public Object get(String key){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    /**
     * 向redis中放入hash数据
     * @author tansitao
     * @time 2018/11/19 11:56 
     */
    public void putHash(String hashName, String hashKey, Object hashValue){
        HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(hashName, hashKey, hashValue);
    }

    /**
     * 将hashMap放入到redis的hash数据结构中
     * @author tansitao
     * @time 2018/11/19 11:58 
     */
    public void putHashMap(String hashName, HashMap<String,Object> hashMap){
        HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.putAll(hashName, hashMap);
    }

    /**
     * 删除redis中hash的某个key值和数据
     * @author tansitao
     * @time 2018/11/19 12:00 
     */
    public boolean delHash(String hashName, String hashKey){
        HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        if(hashOperations.delete(hashName, hashKey) > 0){
            return true;
        }else {
            return false;
        }
    }

    /**
     *  判断Redis中得hash中是否有该值
     * @author tansitao
     * @time 2018/11/19 12:02 
     */
    public boolean hasHashKey(String hashName, String hashKey){
        HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.hasKey(hashName, hashKey);
    }

    /**
      * 从redis中获取hash数据
      * @author tansitao
      * @time 2018/11/19 11:56 
      */
    public Object getHash(String hashName, String hashKey){
        HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(hashName, hashKey);
    }

    /**
      * 从redis中获取hash数组数据
      * @author tansitao
      * @time 2018/11/19 11:56 
      */
    public List multiGetHash(String hashName, List<String>  hashKeys){
        HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.multiGet(hashName, hashKeys);
    }

    /**
      * 原子性增加redis中的hash数据中的值
      * @author tansitao
      * @time 2018/11/19 11:56 
      */
    public Long incrementHash(String hashName, String hashKey, Long addNum){
        return redisTemplate.opsForHash().increment(hashName, hashKey, addNum);
    }

}
