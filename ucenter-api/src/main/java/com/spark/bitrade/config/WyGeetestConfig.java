package com.spark.bitrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.07 15:15  
 */
@Data
@Component
@ConfigurationProperties(prefix = "wy.geetest")
public class WyGeetestConfig {


    private String captchaId;

    private String secretId;

    private String version;

    private String secretKey;

    private String validateUrl;
}
