package com.spark.bitrade.config;

import com.spark.bitrade.h5game.H5Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * H5GameConfig
 *
 * @author archx
 * @time 2019/4/26 14:29
 */
@Configuration
public class H5GameConfig {

    @Bean
    @ConfigurationProperties(prefix = "h5game")
    public H5Config buildH5Config() {
        return new H5Config();
    }
}
