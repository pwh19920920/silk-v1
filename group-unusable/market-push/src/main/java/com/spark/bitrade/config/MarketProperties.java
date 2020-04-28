package com.spark.bitrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/***
 * 自定义参数
 *
 * @author yangch
 * @time 2018.09.05 15:02
 */

@Component
@ConfigurationProperties(prefix="market.custom")
@Data
public class MarketProperties {

    //交易区
    List<String> tradeCategory = new ArrayList<>();

}
