package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingRecordStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.RewardStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 投注记录表DTO
  * @author tansitao
  * @time 2018/9/15 11:53 
  */
@Data
public class BettingRecordDTO {
    private Long id;

    //id,期数id
    private Long periodId;

    //周期/期数
    private String period;

    //投注开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    //投注结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    //投注时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date betTime;

    //投注币种
    private String betSymbol;

    //投注数量
    private BigDecimal betNum;

    //价格范围区间
    private Long rangeId;

    //价格开始范围
    private BigDecimal beginRange;

    //价格结束范围
    private BigDecimal endRange;

    //竞猜币种
    private String guessSymbol;

    //是否订阅开奖短信提醒
    private BooleanEnum useSms;

    //活动状态
    @Enumerated(EnumType.ORDINAL)
    private BettingRecordStatus status;

    //状态,待领取、已领取、过期/失效
    @Enumerated(EnumType.ORDINAL)
    private RewardStatus rewardStatus;

    //奖励币种
    private String symbol;

    //奖励数量
    private BigDecimal rewardNum;

}