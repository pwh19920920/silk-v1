package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员市场奖励业绩总累计表
 * @author Zhang Yanjun
 * @time 2018.12.03 18:37
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockMarketPerformanceTotal {
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private String id;

    private Long memberId;

    @Column(columnDefinition = "decimal(18,8) comment '个人锁仓累计数量'")
    private BigDecimal ownLockAmountTotal=BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(18,8) comment '子部门累计数量'")
    private BigDecimal subDepartmentAmountTotal=BigDecimal.ZERO;

    @Column(columnDefinition = "bigint(20) comment '推荐人id'")
    private Long iniviteId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '创建时间'")
    @CreationTimestamp
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '更新时间'")
    @UpdateTimestamp
    private Date updateTime;

    @Column(columnDefinition = "varchar(24) comment '币种unit名称，如：CNYT'")
    private String symbol;
}
