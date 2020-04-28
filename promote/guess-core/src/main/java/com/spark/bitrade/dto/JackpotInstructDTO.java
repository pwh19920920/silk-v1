package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingConfigStatus;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 奖池说明
  * @author tansitao
  * @time 2018/10/18 16:24 
  */
@Data
public class JackpotInstructDTO {

    //上期沉淀
    private BigDecimal prevJackpotBalanceTotal;

    //本期投票总量
    private BigDecimal betTotalPrize;

}