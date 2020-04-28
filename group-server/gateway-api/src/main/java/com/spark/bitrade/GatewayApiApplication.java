package com.spark.bitrade;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@EnableScheduling
@EnableAutoConfiguration
//@EnableTransactionManagement(order = 10) //开启事务，并设置order值，默认是Integer的最大值
@ComponentScan(basePackages={"com.spark.bitrade","com.spark.bitrade.system"})
@SpringCloudApplication
//@SpringBootApplication
@EnableCaching
@EnableZuulProxy
@ServletComponentScan("com.spark.bitrade.config") //开启Druid监控
//@EnableHystrixDashboard
public class GatewayApiApplication {

    public static void main(String[] args){
        SpringApplication.run(GatewayApiApplication.class,args);
    }
}
