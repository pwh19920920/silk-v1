package com.spark.bitrade.entity;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FxhApiDate {

    //币种代码（唯一主键）
    private  String id;
    //币种英文名称
    private String name;
    //币种的简称 
    private String symbol;
    //最新价格(单位:美元) 
    private String priceUsd;
    //1小时涨跌幅
    private String percentChange1h;
    //24小时涨跌幅
    private String percentChange24h;
    //行情更新时间(10位unix时间戳)
    private String lastUpdated;


}
