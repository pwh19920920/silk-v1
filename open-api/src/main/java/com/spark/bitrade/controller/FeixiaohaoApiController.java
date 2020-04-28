package com.spark.bitrade.controller;


import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.spark.bitrade.client.AgentInterceptor;
import com.spark.bitrade.client.TickerHttpClient;
import com.spark.bitrade.entity.FxhApiDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/v1")
public class FeixiaohaoApiController {

    @Autowired
    private  RedisTemplate redisTemplate;

    @Autowired
    private TickerHttpClient tickerHttpClient;


    /**
     * 最新交易缓存key
     */
    public  final String TRADE_PLATE_MAP = "entity:trade:plateMap:";

    /**
     * 创建定时缓存，默认2分钟过期
     */
    TimedCache<String, HashMap> timedCache = CacheUtil.newTimedCache(120000);

    private final String localCacheKey = "allticker";


        /**
         *   非小号allticker api接口
         *
         */
    @RequestMapping("allticker")
    public  HashMap allticker() {
        RestTemplate restTemplate = new RestTemplate();
        log.info("====================非小号获取 api allticker接口========================");

        //关闭获取参数时延长缓存超时时间
        HashMap<String, Object> resultHashMap = timedCache.get(localCacheKey,false);

        Long cacheUpdateTime;
        if(resultHashMap==null || resultHashMap.isEmpty()){
            resultHashMap = new ManagedMap<>();
            List<LinkedHashMap> coinThumbLicst = tickerHttpClient.queryPrice();
            if (coinThumbLicst != null) {

                ValueOperations valueOperations = redisTemplate.opsForValue();
                //循环遍历所有币种
                for (LinkedHashMap linkThumb : coinThumbLicst) {
                            FxhApiDate fxhApiDate = FxhApiDate.builder().id(linkThumb.get("id").toString())
                                    .name(linkThumb.get("name").toString()).symbol(linkThumb.get("symbol").toString())
                                    .priceUsd(linkThumb.get("price_usd").toString()).percentChange1h(linkThumb.get("percent_change_1h").toString())
                                    .lastUpdated(linkThumb.get("last_updated").toString())
                                    .build();
                            valueOperations.set(TRADE_PLATE_MAP + linkThumb.get("symbol"), fxhApiDate, 12, TimeUnit.HOURS);
                            resultHashMap.put(TRADE_PLATE_MAP + linkThumb.get("symbol"),fxhApiDate);
                }
                //本地缓存
                timedCache.put(localCacheKey, resultHashMap);
                log.info("===非小号获取 api allticker接口===实时返回数据：" + resultHashMap);
            }
        } else {
            log.info("===非小号获取 api allticker接口===本地缓存返回数据：" + resultHashMap);
        }
        return resultHashMap;
    }


}
