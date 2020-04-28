package com.spark.bitrade.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author fumy
 * @since 2018-06-16
 */
@Entity
@Data
@ExcelSheet
public class TraderExchangeFeeStat {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Excel(name = "统计周期")
    @Column(columnDefinition = "varchar(255) comment '统计周期'")
    private String opDate;
    /**
     * 日期（YYYY-MM-DD）
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    /**
     * 交易币
     */
    @Excel(name = "交易币")
    @Column(columnDefinition = "varchar(255) comment '交易币'")
    private String coinUnit;
    /**
     * 定价币
     */
    @Excel(name = "定价币")
    @Column(columnDefinition = "varchar(255) comment '定价币'")
    private String baseUnit;
    /**
     * 总交易量
     */
    @Excel(name = "总交易量")
    @Column(columnDefinition = "decimal(19,8) comment '总交易量'")
    private BigDecimal tradedAmount;
    /**
     * 总交易额
     */
    @Excel(name = "总交易额")
    @Column(columnDefinition = "decimal(19,8) comment '总交易额'")
    private BigDecimal turnover;
    /**
     * 总买家手续费
     */
    @Excel(name = "总买家手续费")
    @Column(columnDefinition = "decimal(19,8) comment '总买家手续费'")
    private BigDecimal buyFee;
    /**
     * 总卖家手续费
     */
    @Excel(name = "总卖家手续费")
    @Column(columnDefinition = "decimal(19,8) comment '总卖家手续费'")
    private BigDecimal saleFee;
    /**
     * 总买家成交额
     */
    @Excel(name = "总买家成交额")
    @Column(columnDefinition = "decimal(19,8) comment '总买家成交额'")
    private BigDecimal saleAmount;
    /**
     * 总卖家成交额
     */
    @Excel(name = "总卖家成交额")
    @Column(columnDefinition = "decimal(19,8) comment '总卖家成交额'")
    private BigDecimal buyAmount;
}
