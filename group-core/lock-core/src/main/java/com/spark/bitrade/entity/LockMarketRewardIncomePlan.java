package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.LockRewardType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 市场奖励返还计划表
 * @author Zhang Yanjun
 * @time 2018.12.03 19:00
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockMarketRewardIncomePlan {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private Long memberId;

    @Column(columnDefinition = "decimal(18,8) comment '奖励金额'")
    private BigDecimal rewardAmount;
    
    @Column(columnDefinition = "int(11) comment '奖励类型(直推奖、级差奖、培养奖)'")
    private LockRewardType rewardType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '返还时间'")
    private Date rewardTime;

    @Column(columnDefinition = "int(11) comment '分期数，从1开始'")
    private int period;

    @Column(columnDefinition = "int(11) comment '状态，0待返还、1返还中、2已返还'")
    private LockBackStatus status;

    @Column(columnDefinition = "bigint(20) comment '关联的活动锁仓记录id'")
    private Long lockDetailId;

    @Column(columnDefinition = "bigint(20) comment '关联的市场奖励明细ID'")
    private Long marketRewardDetailId;

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

    @Column(columnDefinition = "varchar(24) comment '币种unit名称，如：CNYT'")
    private String symbol;
}
