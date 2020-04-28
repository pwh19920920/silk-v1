package com.spark.bitrade.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 外部商户c2c交易手续费统计表
 * </p>
 *
 * @author fumy
 * @since 2018-06-19
 */
@ApiModel
@Entity
@Data
@ExcelSheet
public class C2cOuterFeeStat {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    /**
     * 日期（YYYY-MM-DD）
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    /**
     * 交易日期
     */
    @Excel(name = "统计周期")
    @Column(columnDefinition = "varchar(255) comment '统计周期'")
    private String opDate;
    /**
     * 交易类型，0：买，1：卖
     */
    @Excel(name = "交易类型")
    @Column(columnDefinition = "int(2) comment '交易类型'")
    private Integer type;
    /**
     * 总成交数量
     */
    @Excel(name = "总成交数量")
    @Column(columnDefinition = "decimal(19,8) comment '总成交数量'")
    private BigDecimal tradeAmount;
    /**
     * 总交易额
     */
    @Excel(name = "总交易额")
    @Column(columnDefinition = "decimal(19,8) comment '总交易额'")
    private BigDecimal tradeTurnover;
    /**
     * 手续费
     */
    @Excel(name = "手续费")
    @Column(columnDefinition = "decimal(19,8) comment '手续费'")
    private BigDecimal fee;

    @Excel(name = "定价币种")
    @Column(columnDefinition = "varchar(32) comment '定价币种'")
    private String unit;
}
