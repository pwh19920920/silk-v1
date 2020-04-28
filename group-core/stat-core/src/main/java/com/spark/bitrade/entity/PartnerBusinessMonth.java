package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 区域合伙人业务量月统计
  * @author tansitao
  * @time 2018/5/28 12:04 
  */
@Entity
@Data
@Table
public class PartnerBusinessMonth {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 统计周期
     */
    @Column(columnDefinition = "varchar(32) comment '统计周期'")
    private String statisticalCycle;

    /**
     * 区域id
     */
    @Column(columnDefinition = "varchar(16) comment '区域id'")
    private String areaId;

    /**
     * 本月累计新增用户数
     */
    @Column(columnDefinition = "bigint comment '本月新增用户数'")
    private long monthAddUserNum;

    /**
     * 当月累计交易量（USDT）
     */
    @Column(columnDefinition = "decimal(18,8) comment '当月累计交易量'")
    private BigDecimal monthTradeAmount = BigDecimal.ZERO;

    /**
     * 当月累计收益（USDT）
     */
    @Column(columnDefinition = "decimal(18,8) comment '当月累计收益'")
    private BigDecimal monthIncomeAmount = BigDecimal.ZERO;



}
