package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author fumy
 * @time 2018.09.27 18:14
 */
@ApiModel
@Entity
@Data
public class TotalBalanceStat {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 日期（YYYY-MM-DD）
     */
    @ApiModelProperty(value = "日期",name = "createTime")
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    /**
     * 交易日期
     */
    @Excel(name = "统计周期")
    @Column(columnDefinition = "varchar(255) comment '统计周期'")
    private String opDate;

    @Column(columnDefinition = "varchar(255) comment '中文名'")
    private String nameCn;

    @Column(columnDefinition = "varchar(255) comment '名称（英文）'")
    private String name;

    @Column(columnDefinition = "varchar(255) comment '币种缩写'")
    private String unit;

    @Column(columnDefinition = "decimal(19,8) comment '会员总余额'")
    private BigDecimal allBalance;

    @Column(columnDefinition = "decimal(19,8) comment '钱包余额'")
    private BigDecimal hotAllBalance;

    @Column(columnDefinition = "varchar(255) comment '提币地址'")
    private String coinBaseAddress;

    @Column(columnDefinition = "decimal(19,8) comment '提币地址余额'")
    private BigDecimal coinBaseBalance;

    @Column(columnDefinition = "varchar(255) comment '冷钱包地址'")
    private String coldWalletAddress;
}
