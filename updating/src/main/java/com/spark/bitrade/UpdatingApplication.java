package com.spark.bitrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * Desc:
 * Author: yangch
 * Version: 1.0
 * Create Date Time: 2018-05-24 22:29:00
 * Update Date Time:
 *
 * @see
 */
@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
//@EnableCaching
public class UpdatingApplication {
    public static void main(String[] args) {
        SpringApplication.run(UpdatingApplication.class, args);
    }
}
