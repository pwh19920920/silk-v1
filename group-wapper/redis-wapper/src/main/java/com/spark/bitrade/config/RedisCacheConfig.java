package com.spark.bitrade.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * @author Zhang Jinwei
 * @date 2017年12月29日
 */
@Configuration
@EnableAutoConfiguration
@EnableCaching
public class RedisCacheConfig extends CachingConfigurerSupport {
    @Autowired
    //@Qualifier("redisTemplate00")
    private RedisTemplate redisTemplate;

    //缓存有效时间，单位为秒，默认为1800秒=30分钟
    @Value("${spring.redis.cache.expire:1800}")
    private int cacheExpire;

    /**
     * 缓存管理器.
     *
     * @return
     */
    @Bean(name = {"cacheManager","redisCacheManager"})
    @Primary
    public CacheManager cacheManager() {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        // 设置缓存默认过期时间30分钟（全局的）
        cacheManager.setDefaultExpiration(cacheExpire);
        return cacheManager;
    }

//    /**
//     * 缓存管理器.
//     *
//     * @param redisTemplate
//     * @return
//     */
//    @Bean(name = {"cacheManager","redisCacheManager"})
//    @Primary
//    /*public CacheManager cacheManager(RedisTemplate<?, ?> redisTemplate) {
//        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
//        // 设置缓存默认过期时间30分钟（全局的）
//        cacheManager.setDefaultExpiration(1800);
//        return cacheManager;
//    }*/

    /**
     * RedisTemplate配置
     * @param factory
     * @return
     */
    //del by yangch 时间： 2018.12.21 原因：删除重复的redisTemplate
    /*@Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        //定义key序列化方式
        //RedisSerializer<String> redisSerializer = new StringRedisSerializer();//Long类型会出现异常信息;需要我们上面的自定义key生成策略，一般没必要
        //定义value的序列化方式
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // template.setKeySerializer(redisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }*/

}
