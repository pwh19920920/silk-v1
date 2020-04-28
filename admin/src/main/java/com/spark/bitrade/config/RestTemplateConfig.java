package com.spark.bitrade.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.chrono.JapaneseDate;

@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        //edit by  shenzucai 时间： 2018.08.28  原因：用于暂时性解决后台总额查询读超时
        // todo 后续将进行结果缓存，和动态更新，而不是每次请求动态的去统计
        RestTemplate restTemplate= new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 连接主机的超时时间（单位：毫秒）
        requestFactory.setConnectTimeout(-1);
        // 从主机读取数据的超时时间（单位：毫秒）
        requestFactory.setReadTimeout(-1);
        restTemplate.setRequestFactory(requestFactory);
        return new RestTemplate();
    }
}
