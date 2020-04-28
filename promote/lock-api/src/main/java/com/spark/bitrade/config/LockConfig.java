package com.spark.bitrade.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Map;


/**
 * 锁仓配置信息
 * @author tansitao
 * @time 2018/12/8 16:30 
 */
@Configuration
@Data
public class LockConfig {

    /**
     * 指定币种的锁仓周期
     */
    @Value("#{${lock.cycle.maps}}")
    private Map<String,Integer> cycles;

    /**
     * 默认的锁仓周期
     */
    @Value("${lock.cycle}")
    private int cycle;

    /**
     * 指定币种的锁仓培养奖利率
     */
    @Value("#{${lock.training.rate.maps}}")
    private Map<String,BigDecimal> rates;

    /**
     * 默认的锁仓培养奖利率
     */
    @Value("${lock.training.rate}")
    private BigDecimal rate;

    /**
     * 升级为“实时返佣”的时间（用于根据时间兼容“按月返佣”的历史数据）
     */
    @Value("${lock.update.version.date:2019-03-16 00:00:00}")
    private String updateVersionDate;



    /**
     * 获取指定币种的锁仓周期
     * @param symbol 币种名称
     * @return
     */
    public int getCycle(String symbol){
        return cycles.getOrDefault(symbol, cycle);
    }

    /**
     * 获取指定币种的锁仓培养奖利率
     * @param symbol 币种名称
     * @return
     */
    public BigDecimal getRate(String symbol){
        return rates.getOrDefault(symbol, rate);
    }
}
