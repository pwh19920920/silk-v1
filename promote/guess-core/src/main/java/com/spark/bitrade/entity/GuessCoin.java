package com.spark.bitrade.entity;

import lombok.Data;

/**
 * 竞猜币种
 * @author Zhang Yanjun
 * @time 2018.09.12 15:43
 */

@Data
public class GuessCoin {
    private String id;//币种id （如 bitcoin）
    private String name;//名字（如：Bitcoin）
    private String symbol;//币种（如：BTC）
    private String priceUsd;//美元价格
    private String lastUpdated; //非小号外部接口最后更新时间
    private String lastRedisTime;//最后缓存时间
}
