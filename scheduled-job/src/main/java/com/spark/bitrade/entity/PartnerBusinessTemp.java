package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 区域合伙人业务累计详细临时表
  * @author tansitao
  * @time 2018/5/28 12:04 
  */
@Entity
@Data
@Table
public class PartnerBusinessTemp {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 统计周期
     */
    @Column(columnDefinition = "varchar(32) comment '统计周期'")
    private String statisticalCycle;

    /**
     * 区域信息
     */
    private String areaId;

    /**
     * 当天新增用户数
     */
    private long dayAddUserNum;

    /**
     * 本月累计新增用户数
     */
    private long monthAddUserNum;

    /**
     * 总累计新增用户数
     */
    private long allAddUserNum;

    /**
     * 当天累计交易量（USDT）
     */
    @Column(columnDefinition = "decimal(18,8)")
    private BigDecimal dayTradeAmount;

    /**
     * 当月累计交易量（USDT）
     */
    @Column(columnDefinition = "decimal(18,8)")
    private BigDecimal monthTradeAmount;

    /**
     * 总累计交易量（USDT）
     */
    @Column(columnDefinition = "decimal(18,8)")
    private BigDecimal allTradeAmount;

    /**
     * 当天累计收益（USDT）
     */
    @Column(columnDefinition = "decimal(18,8)")
    private BigDecimal dayIncomeAmount;

    /**
     * 当月累计收益（USDT）
     */
    @Column(columnDefinition = "decimal(18,8)")
    private BigDecimal monthIncomeAmount;

    /**
     * 总累计收益（USDT）
     */
    @Column(columnDefinition = "decimal(18,8)")
    private BigDecimal allIncomeAmount;


}
