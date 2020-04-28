package com.spark.bitrade.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderScreen extends OtcOrderTopScreen{
    private String orderSn;
    private BigDecimal minNumber ;
    private BigDecimal maxNumber ;
    private String memberName;//用户名和真名的关键字即可
    private String customerName;//用户名和真名的关键字即可
    private BigDecimal minMoney;
    private BigDecimal maxMoney;
}
