package com.spark.bitrade;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * kafka消费,0924增加注释
 *
 * @author admin
 * @EnableTransactionManagement(order = 10)  开启事务，并设置order值，默认是Integer的最大值
 * @ServletComponentScan("com.spark.bitrade.config") 开启Druid监控
 * @ComponentScan(basePackages={"com.spark.bitrade"}) 包扫描，已注释暂不使用
 * @date 2019.09.24
 */
@EnableEurekaClient
@EnableScheduling
@EnableAutoConfiguration
@EnableTransactionManagement(order = 10)
@EnableAsync
@SpringBootApplication
@EnableCaching
@ServletComponentScan("com.spark.bitrade.config")
@EnableFeignClients
public class CollectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectApplication.class, args);
    }
}
