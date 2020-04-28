package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.LockBackStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 收益返还计划表 按月返还
 * @author Zhang Yanjun
 * @time 2018.12.03 17:08
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockMemberIncomePlan {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private Long memberId;

    @Column(columnDefinition = "decimal(18,8) comment '返还金额'")
    private BigDecimal amount;

    @Column(columnDefinition = "int(11) comment '分期数，从1开始'")
    private int period;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '到账时间'")
    private Date rewardTime;

    @Column(columnDefinition = "int(11) comment '状态，0待返还、1返还中、2已返还'")
    private LockBackStatus status;

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

    @Column(columnDefinition = "varchar(24) comment '币种unit名称，如：CNYT'")
    private String symbol;
}
