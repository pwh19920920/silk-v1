package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.RewardBusinessType;
import com.spark.bitrade.constant.RewardStatus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 中奖信息
 * @author yangch
 * @time 2018.09.13 11:23
 */

@Data
public class RewardDTO {

    //id,期数id
    private Long periodId;

    //是否参加活动
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isJoin;

    //是否中奖
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isWin;

    //状态,待领取、已领取、过期/失效
    @Enumerated(EnumType.ORDINAL)
    private RewardStatus winStatus;

    //是否开启过红包
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isOpenRed;

    //状态,未中奖、已中奖
    @Enumerated(EnumType.ORDINAL)
    private RewardStatus redStatus;

    //中奖数量
    private BigDecimal rewardNum = BigDecimal.ZERO;

    //开红包数量
    private BigDecimal openRedNum = BigDecimal.ZERO;

    //奖励币种
    private String prizeSymbol;

    //红包奖励币种
    private String redpacketPrizeSymbol;

    //是否为手气最佳
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isBestLuck;
}