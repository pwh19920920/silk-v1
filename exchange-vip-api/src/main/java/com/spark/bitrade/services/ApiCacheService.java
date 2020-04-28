package com.spark.bitrade.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * <p>api缓存管理服务</p>
 * @author tian.bo
 * @date 2018-12-7
 */
@Service
@Slf4j
public class ApiCacheService {
    @CacheEvict(value="apikey",allEntries=true)
    public void clearCache(){
        log.info("-----正在清除apikey缓存----");
    }
}
