package com.spark.bitrade.model.promote;


import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author fumy
 * @time 2018.09.13 14:29
 */
@Data
public class BettingConfigScreen{


    private Long id;

    //周期/期数
    private String period;

    //活动名称
    private String name;


    private Date beginTime;

    private Date endTime;

    private Date openTime;


    private Date prizeBeginTime;

    private Date prizeEndTime;


    private Date redpacketBeginTime;

    private Date redpacketEndTime;

    private BettingConfigStatus status = BettingConfigStatus.STAGE_PREPARE;

    //备注
    private String remark;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;


    private BooleanEnum deleted = BooleanEnum.IS_FALSE;


    private BigDecimal prizePrice;
    private String betSymbol;

    //起投数量限制
    private BigDecimal lowerLimit;

    private String guessSymbol;

    private String prizeSymbol;
    private String redpacketSymbol;


    private BigDecimal redpacketGradeRatio;

    private BigDecimal redpacketUseNum;

    private BigDecimal rebateRatio;

    private BigDecimal prizeRatio;


    private BigDecimal backRatio;

    private BigDecimal redpacketRatio;

    private BigDecimal nextPeriodRatio;

    private BooleanEnum redpacketState;

    private int redpacketOpenLimit;

    private int redpacketCoefficientRatio;

    private String redpacketPrizeSymbol;

    private String smsSymbol;

    private BigDecimal smsUseNum;

}
