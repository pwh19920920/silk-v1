package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.PromotionRewardCycle;
import com.spark.bitrade.constant.RewardRecordLevel;
import com.spark.bitrade.constant.RewardRecordStatus;
import com.spark.bitrade.constant.RewardRecordType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 奖励记录
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class RewardRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @JoinColumn(name = "coin_id", nullable = false)
    @ManyToOne
    private Coin coin;
    private String  remark;
    @Enumerated(EnumType.ORDINAL)
    private RewardRecordType type;
    @Column(columnDefinition = "decimal(18,8) comment '数目'")
    private BigDecimal amount;
    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne
    private Member member;
    /**
     * 创建时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //add by yangch 时间： 2018.05.16 原因：扩展字段
    private Long  refTransactionId; //关联交易记录ID，引用member_transaction表

    @Enumerated(EnumType.ORDINAL)
    private RewardRecordLevel level; //返佣级别（1级2级3级）

    @Enumerated(EnumType.ORDINAL)
    private RewardRecordStatus status; //返佣状态（未发放、发放中、已发放）

    //@CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date treatedTime; //返佣时间

    //add by yangch 时间： 2018.05.29 原因：添加 奖励的来源会员ID
    //奖励的来源会员ID
    /*@JoinColumn(name = "from_member_id")
    @ManyToOne
    private Member fromMemberId;*/
    /**
     * 奖励的来源会员ID
     */
    private Long fromMemberId;

    //add by yangch 时间： 2018.05.29 原因：添加 奖励的来源币种
    //奖励的来源币种，如SLB
    private String fromCoinUnit;

    //add by yangch 时间： 2018.05.29 原因：添加 奖励的来源数目（即：金额）
    //奖励的来源数目（即：金额）
    @Column(columnDefinition = "decimal(18,8) comment '来源数目'")
    private BigDecimal fromAmount;

    //add by yangch 时间： 2018.05.29 原因：添加 奖励的来源数目转换为目标币种的汇率
    //兑换汇率
    @Column(columnDefinition = "decimal(18,8) comment '兑换汇率'")
    private BigDecimal exchangeRate;

    //add by yangch 时间： 2018.05.29 原因：返佣周期的冗余字段（实时、天、周、月）
    @Enumerated(EnumType.ORDINAL)
    private PromotionRewardCycle rewardCycle;
}
