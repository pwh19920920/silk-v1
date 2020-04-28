package com.spark.bitrade.dto;

import com.spark.bitrade.constant.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.09 18:01  
 */
@Data
public class FeeOtcExchangeDto {

    private BigDecimal fee;

    private String coinUnit;

    private TransactionType transactionType;
}
