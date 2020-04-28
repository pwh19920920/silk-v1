package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lingxing
 * @time 2018.07.17 19:08
 */
@Setter
@Getter
public class TechRechargeRecordDto {
    private  String coinUnit;
    private  String memberAccount;
    private String startTime;
    private String endTime;
    private  BigDecimal startRechargeNumber;
    private BigDecimal endRechargeNumber;
}
