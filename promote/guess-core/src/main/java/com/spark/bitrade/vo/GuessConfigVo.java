package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 竞猜活动
  * @author tansitao
  * @time 2018/9/13 11:02 
  */
@Data
@Builder
public class GuessConfigVo {

    private Long id;

    //周期/期数
    private String period;

    //活动名称
    private String name;

    //投注开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;

    //投注结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    //开奖时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date openTime;

    //领奖开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date prizeBeginTime;

    //领奖结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date prizeEndTime;

    //红包领取开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date redpacketBeginTime;

    //红包领取结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date redpacketEndTime;

    //活动状态
    @Enumerated(EnumType.ORDINAL)
    private BettingConfigStatus status = BettingConfigStatus.STAGE_PREPARE;

    //备注
    private String remark;

    //中奖价格
    private BigDecimal prizePrice;

    //投注币种
    private String betSymbol;

    //起投数量限制
    private BigDecimal lowerLimit;

    //竞猜币种
    private String guessSymbol;

    //奖励币种
    private String prizeSymbol;

    //红包支付币种
    private String redpacketSymbol;

    //大红包设定比例
    private BigDecimal redpacketGradeRatio;

    //红包币种支付数量
    private BigDecimal redpacketUseNum;

    //返佣比例
    private BigDecimal rebateRatio;

    //奖励比例
    private BigDecimal prizeRatio;

    //回购比例
    private BigDecimal backRatio;

    //红包比例
    private BigDecimal redpacketRatio;

    //下期奖池沉淀比例
    private BigDecimal nextPeriodRatio;

    //奖池余量
    private BigDecimal jackpotBalance;

    //红包余量
    private BigDecimal redpacketBalance;

    //红包奖励币种
    private String redpacketPrizeSymbol;

    //是否开启红包
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum redpacketState;
}
