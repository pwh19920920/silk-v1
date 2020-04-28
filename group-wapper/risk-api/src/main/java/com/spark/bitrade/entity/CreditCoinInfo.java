package com.spark.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 充币事件信息
 * @author Zhang Yanjun
 * @time 2018.12.27 10:48
 */
@Data
public class CreditCoinInfo {
    /**
     * 币种
     */
    private String coin;
    /**
     * 数量
     */
    private BigDecimal amount;
    /**
     * 来源地址
     */
    private String source;
    /**
     * 到账地址
     */
    private String target;
}
