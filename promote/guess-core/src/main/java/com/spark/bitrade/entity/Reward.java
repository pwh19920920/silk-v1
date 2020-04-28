package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.RewardBusinessType;
import com.spark.bitrade.constant.RewardStatus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 中奖记录表
 * @author yangch
 * @time 2018.09.13 11:23
 */

@Entity
@Data
@Table(name = "pg_reward")
public class Reward {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //id,期数id
    private Long periodId;

    @Transient
    //周期/期数
    private String period;

    //红包支付币种
    @Transient
    private String redpacketSymbol;

    //大红包设定比例
    @Transient
    private BigDecimal redpacketGradeRatio;

    //红包币种支付数量
    @Transient
    private BigDecimal redpacketUseNum;

    //用户昵称
//    @Transient
//    private String username;

    //奖励数量
    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
    private BigDecimal rewardNum;

    //奖励币种
    @Column(columnDefinition = "varchar(32) comment '奖励币种'")
    private String symbol;

    //业务类型,竞猜、抢红包
    @Enumerated(EnumType.ORDINAL)
    private RewardBusinessType businessType;

    //中奖会员
    private Long memberId;

    //会员推荐码
    @Column(columnDefinition = "varchar(32) comment '会员推荐码'")
    private String promotionCode;

    //状态,待领取、已领取、过期/失效
    @Enumerated(EnumType.ORDINAL)
    private RewardStatus status;

    //领取时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date getTime;

    //是否为手气最佳
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isBestLuck;

    //版本号,解决幂等性
    private int version;

    //投注id
    private Long refId;

}