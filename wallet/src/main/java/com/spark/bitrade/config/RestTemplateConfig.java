package com.spark.bitrade.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {

        //add|edit|del by shenzucai 时间： 2018.05.21 原因：处理超时问题 开始
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(-1);
        requestFactory.setReadTimeout(-1);
        restTemplate.setRequestFactory(requestFactory);
        //add|edit|del by shenzucai 时间： 2018.05.21 原因：处理超时问题 结束
        return restTemplate;
    }
}
