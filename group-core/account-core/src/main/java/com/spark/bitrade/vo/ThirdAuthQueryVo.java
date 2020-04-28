package com.spark.bitrade.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author fumy
 * @time 2018.09.19 16:27
 */
@Data
public class ThirdAuthQueryVo {

    //授权币种
    private String symbol;

    //充提币地址
    private String address;

    //充提实际数量
    private BigDecimal arrivedAmount;

    //充提时间
    private Date createTime;

}
