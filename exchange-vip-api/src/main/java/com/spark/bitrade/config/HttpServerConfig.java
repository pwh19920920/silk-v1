package com.spark.bitrade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * <p>HttpServerConfig</p>
 * @author tian.bo
 * @date 2018/12/5
 */
@Configuration
@Component
public class HttpServerConfig {

    @Value("${api.url}")
    private String apiUrl;

    @Value("${api.version}")
    private String apiVersion;

    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiVersion(){
        return apiVersion;
    }

}
