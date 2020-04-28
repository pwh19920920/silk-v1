package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.PartnerLevle;
import com.spark.bitrade.constant.PartnerStaus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 区域合伙人业务累计详细
  * @author tansitao
  * @time 2018/5/28 12:04 
  */
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartnerBusiness {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 统计周期
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "varchar(32) comment '统计周期'")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date statisticalCycle;

    /**
     * 区域信息
     */
    @Column(columnDefinition = "varchar(16) comment '区域id'")
    private String areaId;

    /**
     * 当天新增用户数
     */
    @Column(columnDefinition = "bigint comment '当天新增用户数'")
    private long dayAddUserNum;

    /**
     * 本月累计新增用户数
     */
    @Column(columnDefinition = "bigint comment '本月新增用户数'")
    private long monthAddUserNum;

    /**
     * 总累计新增用户数
     */
    @Column(columnDefinition = "bigint comment '总累新增用户数'")
    private long allAddUserNum;

    /**
     * 当天累计交易量（USDT）
     */
    @Column(columnDefinition = "decimal(18,8) comment '当天累计交易量'")
    private BigDecimal dayTradeAmount = BigDecimal.ZERO;

    /**
     * 当月累计交易量（USDT）
     */
    @Column(columnDefinition = "decimal(18,8) comment '当月累计交易量'")
    private BigDecimal monthTradeAmount = BigDecimal.ZERO;

    /**
     * 总累计交易量（USDT）
     */
    @Column(columnDefinition = "decimal(18,8) comment '累计交易量'")
    private BigDecimal allTradeAmount = BigDecimal.ZERO;

    /**
     * 当天累计收益（USDT）
     */
    @Column(columnDefinition = "decimal(18,8) comment '当天累计收益'")
    private BigDecimal dayIncomeAmount = BigDecimal.ZERO;

    /**
     * 当月累计收益（USDT）
     */
    @Column(columnDefinition = "decimal(18,8) comment '当月累计收益'")
    private BigDecimal monthIncomeAmount = BigDecimal.ZERO;

    /**
     * 总累计收益（USDT）
     */
    @Column(columnDefinition = "decimal(18,8) comment '总累计收益'")
    private BigDecimal allIncomeAmount = BigDecimal.ZERO;


}
