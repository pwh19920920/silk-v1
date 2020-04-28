package com.spark.bitrade.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author tian.b
 * Redis Configuration 支持多数据库切换
 *     调用时注入不同的redisTemplate，如redisTemplate00，redisTemplate01
 * Created in 2018/8/18.
 */

@Configuration
@EnableCaching
public class RedisConfig {

    //集群模式,哨兵机制生效
    /*@Value("${spring.redis.sentinel.group}")
    protected String sentinelGroupName;*/
    //集群模式,哨兵机制生效
    /*@Value("${spring.redis.sentinel.nodes}")
    protected String sentinelNodes;*/

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.pool.max-idle:10}")
    private int maxIdle;
    @Value("${spring.redis.pool.min-idle:0}")
    private int minIdle;
    @Value("${spring.redis.timeout:1000000}") //超时时间，单位毫秒
    private int timeout;
    @Value("${spring.redis.keytimeout:0}")
    private int keytimeout;


    //del by yangch 时间： 2018.12.21 原因：在RedisCacheConfig类中配置
//    @Bean(name = {"cacheManager","redisCacheManager"})
//    @Primary //配置多个cacheManager解决，多个数据库的问题
//    public CacheManager cacheManager() {
//        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate00());
//        // 设置缓存默认过期时间30分钟（全局的）
//        cacheManager.setDefaultExpiration(1800);
//        return cacheManager;
//    }

    /**
     * 集群配置
     * @return
     */
   /* @Bean
    public RedisSentinelConfiguration redisSentinelConfiguration() {
        String[] nodes = sentinelNodes.split(",");
        Set<String> setNodes = new HashSet<>();
        for (String n : nodes) {
            setNodes.add(n.trim());
        }
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration(sentinelGroupName, setNodes);
        return configuration;
    }*/

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxIdle(maxIdle);
        return poolConfig;
    }

   /* @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }*/

    @Primary
    @Bean(name = "redisTemplate00")
    public StringRedisTemplate redisTemplate00() {
        return buildRedisTemplate(buildConnectionFactory(0));
    }

    @Bean(name = "redisTemplate01")
    public StringRedisTemplate redisTemplate01() {
        return buildRedisTemplate(buildConnectionFactory(1));
    }

  /*  @Bean(name = "redisTemplate02")
    public StringRedisTemplate redisTemplate02() {
        return buildRedisTemplate(buildConnectionFactory(2));
    }*/

   /* @Bean(name = "redisTemplate03")
    public StringRedisTemplate redisTemplate03() {
        return buildRedisTemplate(buildConnectionFactory(3));
    }*/

  /*  @Bean(name = "redisTemplate04")
    public StringRedisTemplate redisTemplate04() {
        return buildRedisTemplate(buildConnectionFactory(4));
    }*/

    private JedisConnectionFactory buildConnectionFactory(int database) {
        //集群模式
        //JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisSentinelConfiguration(),jedisPoolConfig());

        //单机模式
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(jedisPoolConfig());
        connectionFactory.setHostName(host);
        connectionFactory.setPort(port);

        connectionFactory.setUsePool(true);
        connectionFactory.setTimeout(timeout);
        connectionFactory.setDatabase(database);
        connectionFactory.setPassword(password);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    protected StringRedisTemplate buildRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        //使用Jackson库将对象序列化为JSON字符串
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
