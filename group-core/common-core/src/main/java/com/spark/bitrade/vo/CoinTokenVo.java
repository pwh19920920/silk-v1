package com.spark.bitrade.vo;

import lombok.Data;

import javax.persistence.Entity;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fumy
 * @time 2018.09.05 10:09
 */
@Entity
@Data
public class CoinTokenVo implements Serializable{

    private Long id;                    //编号

    private String coinUnit;            //代币单位

    private String baseCoisUnit;        //主币单位

    private int collection;           //是否归集，0：归集，1：不归集

    private BigDecimal colThreshold; //归集阈值

    private String colAddress;      //归集地址

    private int colCycle;           //归集周期

    private String colCron;         //日期配置

    private String contractAddress; //代币合约地址

    private int coinDecimals;    //代币精度

    private String coinName;    //代币名称

    private int status; //状态，0：可用，其他不可用
}
