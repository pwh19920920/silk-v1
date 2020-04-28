package com.spark.bitrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableAsync
@EnableDiscoveryClient
//@EnableSwagger2
//@EnableScheduling
//@EnableAutoConfiguration
//@EnableTransactionManagement(order = 10) //开启事务，并设置order值，默认是Integer的最大值
////@ComponentScan(basePackages={"com.spark.bitrade"})
@SpringBootApplication
//@EnableCaching
//@ServletComponentScan("com.spark.bitrade.config") //开启Druid监控
public class SmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmsApplication.class, args);
    }
}
