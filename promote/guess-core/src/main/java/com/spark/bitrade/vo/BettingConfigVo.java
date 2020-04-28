package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * 投注配置信息Vo
 * @author Zhang Yanjun
 * @time 2018.09.13 16:22
 */
@Data
public class BettingConfigVo {
    private Long id;

    //周期/期数
    private String period;

    //活动名称
    private String name;

    //投注币种
    private String betSymbol;

    //竞猜币种
    private String guessSymbol;

    //奖励币种
    private String prizeSymbol;

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

    //是否删除
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum deleted = BooleanEnum.IS_FALSE;
}
