package com.spark.bitrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/***
 * 测试参数配置
 * @author yangch
 * @time 2018.08.30 17:04
 */

@RefreshScope
@Component
@ConfigurationProperties(prefix="user.tester")
@Data
public class TesterPropertis {
    //白名单用户id
    private List<Integer> whitelist = new ArrayList<>();

}
