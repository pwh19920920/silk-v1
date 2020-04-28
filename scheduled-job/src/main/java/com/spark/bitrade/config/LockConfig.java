package com.spark.bitrade.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 锁仓配置
 * @author tansitao
 * @time 2018.07.19 10:18
 */
@Configuration
@ConfigurationProperties("lock")
@Data
public class LockConfig {

    private int unlockNum;

    @Value("${lock.cycle}")
    private int cycle;

    @Value("${lock.training.rate}")
    private BigDecimal rate;

    /**
     * 升级为“实时返佣”的时间（用于根据时间兼容“按月返佣”的历史数据）
     */
    @Value("${lock.update.version.date:2019-03-16 00:00:00}")
    private String updateVersionDate;

}
