package com.spark.bitrade.config;

import com.spark.bitrade.util.MessageResult;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Zhang Jinwei
 * @date 2018年02月27日
 */
@Configuration
public class RestTemplateConfig {

    /*@Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(5000);//单位为ms
        factory.setConnectTimeout(5000);//单位为ms
        return factory;
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
        return new RestTemplate(factory);
    }*/

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {

        //add|edit|del by  shenzucai 时间： 2018.05.29  原因：添加超时 start
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(-1);
        requestFactory.setReadTimeout(-1);
        restTemplate.setRequestFactory(requestFactory);
        //add|edit|del by  shenzucai 时间： 2018.05.29  原因：添加超时 end
        return restTemplate;
    }

}
