package com.spark.bitrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
  * 全局配置
  * @author tansitao
  * @time 2018/5/11 10:26 
  */
@Configuration
@ConfigurationProperties("global")
public class GlobalConfig {

    private int IdCardSwitch;

    public int getIdCardSwitch() {
        return IdCardSwitch;
    }

    public void setIdCardSwitch(int idCardSwitch) {
        IdCardSwitch = idCardSwitch;
    }
}
