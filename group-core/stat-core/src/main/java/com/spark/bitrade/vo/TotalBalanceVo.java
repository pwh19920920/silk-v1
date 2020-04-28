package com.spark.bitrade.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author fumy
 * @time 2018.10.18 15:25
 */
@Data
public class TotalBalanceVo {

    private BigDecimal hotAllBalance;//钱包余额

    private BigDecimal allBalance;  //会员总余额
}
