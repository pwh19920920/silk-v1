package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.LockRewardType;
import com.spark.bitrade.constant.ProcessStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 市场奖励明细表
 * @author Zhang Yanjun
 * @time 2018.12.03 18:42
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockMarketRewardDetail {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private Long memberId;

    @Column(columnDefinition = "int(11) comment '奖励类型(直推奖、级差奖、培养奖)'")
    private LockRewardType rewardType;

    @Column(columnDefinition = "decimal(18,8) comment '奖励金额'")
    private BigDecimal rewardAmount;

    @Column(columnDefinition = "decimal(18,8) comment '业绩数量'")
    private BigDecimal performanceTurnover;

    @Column(columnDefinition = "bigint(20) comment '会员当前等级id，引用：级差等级配置管理表 lock_reward_level_config.member_level_id'")
    private Long currentLevelId;

    @Column(columnDefinition = "decimal(8,4) comment '绩效系数'")
    private BigDecimal currentPerFactor;

    @Column(columnDefinition = "decimal(8,4) comment '奖励率'")
    private BigDecimal currentRewardRate;

    @Column(columnDefinition = "bigint(20) comment '关联的活动锁仓记录id'")
    private Long lockDetailId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '创建时间'")
    @CreationTimestamp
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '更新时间'")
    @UpdateTimestamp
    private Date updateTime;

    @Column(columnDefinition = "varchar(255) comment '记录校验码'")
    private String code;

    @Column(columnDefinition = "varchar(255) comment '备注'")
    private String comment;

    @Column(columnDefinition = "int(11) comment '记录状态（未处理、处理中、已处理）'")
    private ProcessStatus recordStatus;

    @Column(columnDefinition = "int(11) comment '业绩更新状态（未处理、处理中、已处理）'")
    private ProcessStatus perUpdateStatus;

    @Column(columnDefinition = "int(11) comment '等级更新状态（未处理、处理中、已处理）'")
    private ProcessStatus levUpdateStatus;

    @Column(columnDefinition = "bigint(20) comment '被推荐用户id'")
    private Long refInviteeId;

    @Column(columnDefinition = "bigint(20) comment '关联的锁仓用户ID'")
    private Long refLockMemberId;

    @Column(columnDefinition = "int(11) comment '锁仓天数'")
    private int lockDays;

    @Column(columnDefinition = "datetime comment '锁仓时间'")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lockTime;

    @Column(columnDefinition = "bigint(20) comment '推荐用户ID'")
    private Long inviterId;

    @Column(columnDefinition = "decimal(18,8) comment '总锁仓币数'")
    private  BigDecimal totalAmount;

    @Column(columnDefinition = "decimal(8,4) comment '子部门中的最大奖励率'")
    private BigDecimal subMaxRewardRate;

    @Column(columnDefinition = "int(11) comment '培养奖出现次数'")
    private int trainingCount;

    @Column(columnDefinition = "varchar(24) comment '币种unit名称，如：CNYT'")
    private String symbol;

}
