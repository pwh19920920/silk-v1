package com.spark.bitrade.config;

import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.net.URISyntaxException;

/**
 * @author shenzucai
 * @time 2018.06.22 10:17
 */
@Configuration
public class EhCacheConfig {

    @Bean()
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean(){
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cacheManagerFactoryBean.setShared(true);
        return cacheManagerFactoryBean;
    }

    @Bean("ehCacheCacheManager")
    public EhCacheCacheManager ehCacheCacheManager(EhCacheManagerFactoryBean ehCacheManagerFactoryBean) throws URISyntaxException {
        return new EhCacheCacheManager(ehCacheManagerFactoryBean.getObject());
    }

    /*@Bean("ehCacheCacheManager")
    public EhCacheCacheManager ehCacheCacheManager() throws URISyntaxException {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cacheManagerFactoryBean.setShared(true);
        return new EhCacheCacheManager(cacheManagerFactoryBean.getObject());
    }*/
}
