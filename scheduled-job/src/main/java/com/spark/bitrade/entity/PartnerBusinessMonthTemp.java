package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 区域合伙人业务量月统计临时表
  * @author tansitao
  * @time 2018/5/28 12:04 
  */
@Entity
@Data
@Table
public class PartnerBusinessMonthTemp {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 统计周期
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    @Column(columnDefinition = "varchar(32) comment '统计周期'")
    private Date statisticalCycle;

    /**
     * 区域id
     */
    private String areaId;

    /**
     * 本月累计新增用户数
     */
    private long monthAddUserNum;

    /**
     * 当月累计交易量（USDT）
     */
    @Column(columnDefinition = "decimal(18,8)")
    private BigDecimal monthTradeAmount;

    /**
     * 当月累计收益（USDT）
     */
    @Column(columnDefinition = "decimal(18,8)")
    private BigDecimal monthIncomeAmount;



}
