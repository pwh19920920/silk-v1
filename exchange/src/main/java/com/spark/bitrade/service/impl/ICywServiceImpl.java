package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.service.ICywService;
import com.spark.bitrade.service.optfor.RedisHashService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *  
 *
 * @author young
 * @time 2019.09.11 13:36
 */
@Slf4j
@Service
public class ICywServiceImpl implements ICywService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisHashService redisHashService;


    @Override
    public List<ExchangeOrder> openOrders(String symbol) {
        //查询所有订单
        List<ExchangeOrder> lst = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(getCywOrderTradingKeys(symbol));
        log.info("查询正在交易的机器人用户，交易对={}，用户数量={}", symbol, keys.size());
        keys.forEach(key -> {
            redisHashService.hValues(key).forEach(c -> {
                lst.add((ExchangeOrder) c);
            });
        });

        return lst;
    }


    /**
     * 获取交易订单缓存keys
     *
     * @param symbol 交易对，eg：BTC/USDT
     * @return key=data:cywOrder:t:交易对:用户ID
     */
    static String getCywOrderTradingKeys(String symbol) {
        //key=data:cywOrder:<交易对>:<用户ID>
        return new StringBuilder("data:cywOrder:t:")
                .append(symbol.replace("/", ""))
                .append(":*")
                .toString();
    }
}
