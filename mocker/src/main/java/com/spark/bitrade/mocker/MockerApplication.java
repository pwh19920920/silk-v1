package com.spark.bitrade.mocker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MockerApplication {
	public static void main(String[] args) {
		SpringApplication.run(MockerApplication.class, args);
	}
}
