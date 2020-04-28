package com.spark.bitrade.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/***
 * Redis计数器服务
 * @author yangch
 * @time 2018.11.05 9:44
 */
@Service
public class RedisCountorService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;
    /**
     * 默认过期时长，单位：秒
     */
    private static final long DEFAULT_EXPIRE = 60 * 60 * 24;


    /**
     *
     * @param key
     * @param incrStep
     * @param timeout 超时时间
     * @param unit 超时时间的单位
     * @return
     */
    public long increment(final String key, final long incrStep,
                          final long timeout, final TimeUnit unit){
        Long current = this.redisTemplate.boundValueOps(key).increment(incrStep);
        if (current.longValue() == 1L) {
            this.redisTemplate.expire(key, timeout, unit);
        }
        return current;
    }

    /**
     *
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    public long increment(final String key,
                          final long timeout, final TimeUnit unit){
        return increment(key, 1, timeout, unit);
    }

    /**
     * 进行原子性加减redis中hash中int值
     * @author tansitao
     * @time 2018/11/20 9:01 
     */
    public Long addOrSubtractHash(String hashName, String hashKey, Long number){
        return redisService.incrementHash(hashName, hashKey, number);
    }

    /**
      * 进行原子性将redis中hash值减1
      * @author tansitao
      * @time 2018/11/20 9:01 
      */
    public Long subtractHash(String hashName, String hashKey){
        Integer onlineNum = (Integer) redisService.getHash(hashName, hashKey);
        //add by tansitao 时间： 2018/11/20 原因：如果已经为0或不存在，则不减
        Long current = 0L;
        if(onlineNum != null && onlineNum > 0){
            current = addOrSubtractHash(hashName, hashKey, -1L);
        }
        return current;
    }

}
