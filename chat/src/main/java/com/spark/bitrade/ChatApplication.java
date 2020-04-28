package com.spark.bitrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.annotation.WebFilter;

@EnableEurekaClient
@EnableScheduling
@EnableTransactionManagement(order = 10) //开启事务，并设置order值，默认是Integer的最大值
//@ComponentScan(basePackages={"com.spark.bitrade"})
@SpringBootApplication
@EnableCaching
@EnableAsync
@ServletComponentScan("com.spark.bitrade.config") //开启Druid监控
@WebFilter(asyncSupported = true)
public class ChatApplication {
    public static void main(String[] args){
        SpringApplication.run(ChatApplication.class,args);
    }
}
