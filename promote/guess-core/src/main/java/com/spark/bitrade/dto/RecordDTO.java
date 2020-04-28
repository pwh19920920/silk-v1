package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.BettingRecordStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.RewardStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 每期中奖信息DTO
  * @author tansitao
  * @time 2018/9/15 11:53 
  */
@Data
public class RecordDTO {
    //id,期数id
    private Long periodId;

    //周期/期数
    private String period;

    //奖励币种
    private String prizeSymbol;

    //中奖价格
    private BigDecimal prizePrice;

    //开奖时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date openTime;

    //活动状态
    @Enumerated(EnumType.ORDINAL)
    private BettingConfigStatus status;

    //价格范围区间
    private Long rangeId;

    //价格开始范围
    private BigDecimal beginRange;

    //价格结束范围
    private BigDecimal endRange;

    //中奖人数
    private int peopleNum;

    //奖池数量
    private BigDecimal rewardNum;

    //投注币种
    private String betSymbol;

    //本期红包奖励
    private BigDecimal redpacketAmount;

    //本期推荐分红
    private BigDecimal rewardAmount;

    //本期SLU回购
    private BigDecimal backAmount;

    //本期奖池沉淀
    private BigDecimal jackpotPrecipitation;

    //组名
    private String groupName;

}