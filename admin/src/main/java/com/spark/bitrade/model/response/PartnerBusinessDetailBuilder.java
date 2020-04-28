package com.spark.bitrade.model.response;

import com.spark.bitrade.constant.RewardRecordStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;


import java.math.BigDecimal;

/**
  * 合伙人详细收益查询返回数据
  * @author tansitao
  * @time 2018/5/28 18:16 
  */
@Builder
@Data
public class PartnerBusinessDetailBuilder {
    private String userName; //用户昵称
    private String symbol; //币种
    private Date time; //交易时间
    private BigDecimal incomeAmount; //收益金额
    private RewardRecordStatus status; // 发放状态

}
