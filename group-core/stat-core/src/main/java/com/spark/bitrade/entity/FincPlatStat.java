package com.spark.bitrade.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;

/**
  * 货币统计实体类
  * @author tansitao
  * @time 2018/5/14 15:41 
  */
@Entity
@Data
@Table(name = "FincPlatStat")
@ExcelSheet
public class FincPlatStat{
    /**
     * 主键
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 日期（YYYY-MM-DD）
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date time;

    @Excel(name = "统计周期")
    private String date;
    /**
     * 币种
     */
    @Excel(name = "币种")
    private String unit;
    /**
     * 操盘手人工充值
     */
    @Excel(name = "操盘手人工充值")
    @Column(columnDefinition = "decimal(19,8) comment '操盘手人工充值'")
    private BigDecimal traderTotal;
    /**
     * 内部商户人工充值
     */
    @Excel(name = "内部商户人工充值")
    @Column(columnDefinition = "decimal(19,8) comment '内部商户人工充值'")
    private BigDecimal innerBusinessTotal;
    /**
     * 员工商户人工充值
     */
    @Excel(name = "员工商户人工充值")
    @Column(columnDefinition = "decimal(19,8) comment '员工商户人工充值'")
    private BigDecimal employeeBusinessTotal;
    /**
     * 活动赠送
     */
    @Excel(name = "活动赠送")
    @Column(columnDefinition = "decimal(19,8) comment '活动赠送'")
    private BigDecimal activityTotal;
    /**
     * 推广赠送
     */
    @Excel(name = "推广赠送")
    @Column(columnDefinition = "decimal(19,8) comment '推广赠送'")
    private BigDecimal promotionTotal;
    /**
     * 技术提币数
     */
    @Excel(name = "技术提币数")
    @Column(columnDefinition = "decimal(19,8) comment '技术提币数'")
    private BigDecimal skillTotal;
    /**
     * 内部互转数
     */
    @Excel(name = "内部互转数")
    @Column(columnDefinition = "decimal(19,8) comment '内部互转数'")
    private BigDecimal innerTransTotal;
    /**
     * 内部互转手续费数
     */
    @Excel(name = "内部互转手续费数")
    @Column(columnDefinition = "decimal(19,8) comment '内部互转手续费数'")
    private BigDecimal innerTransFeeTotal;
    /**
     * 客户带币进入数
     */
    @Excel(name = "客户带币进入数")
    @Column(columnDefinition = "decimal(19,8) comment '客户带币进入数'")
    private BigDecimal custDepositTotal;
    /**
     * 客户提币数
     */
    @Excel(name = "客户提币数")
    @Column(columnDefinition = "decimal(19,8) comment '客户提币数'")
    private BigDecimal custWithdrawTotal;
    /**
     * 客户提币手续费数
     */
    @Excel(name = "客户提币手续费数")
    @Column(columnDefinition = "decimal(19,8) comment '客户提币手续费数'")
    private BigDecimal custWithdrawFeeTotal;

    // /**
    //  * 平台买利润
    //  */
    // @Excel(name = "平台买利润")
    // @Column(columnDefinition = "decimal(19,8) comment '平台买利润'")
    // private BigDecimal buyProfit;
    //
    //
    // /**
    //  * 平台买总利润
    //  */
    // @Excel(name = "平台买总利润")
    // @Column(columnDefinition = "decimal(19,8) comment '平台买总利润'")
    // private BigDecimal buyTotalProfit;
    //
    // /**
    //  * 平台卖利润
    //  */
    // @Excel(name = "平台卖利润")
    // @Column(columnDefinition = "decimal(19,8) comment '平台卖利润'")
    // private BigDecimal sellProfit;
    //
    //
    // /**
    //  * 平台卖总利润
    //  */
    // @Excel(name = "平台卖总利润")
    // @Column(columnDefinition = "decimal(19,8) comment '平台卖总利润'")
    // private BigDecimal sellTotalProfit;

    /**
     * 平台总币数
     */
    @Excel(name = "平台总币数")
    @Column(columnDefinition = "decimal(19,8) comment '平台总币数'")
    private BigDecimal platAllTotal;
    /**
     * 平台操盘总币数
     */
    @Excel(name = "平台操盘总币数")
    @Column(columnDefinition = "decimal(19,8) comment '平台操盘总币数'")
    private BigDecimal platTraderTotal;
    /**
     * 平台内部商户总币数
     */
    @Excel(name = "平台内部商户总币数")
    @Column(columnDefinition = "decimal(19,8) comment '平台内部商户总币数'")
    private BigDecimal platInnerTotal;
    /**
     * 平台员工总币数
     */
    @Excel(name = "平台员工总币数")
    @Column(columnDefinition = "decimal(19,8) comment '平台员工总币数'")
    private BigDecimal platEmployeeTotal;

    //钱包币数
    @Excel(name = "钱包币数")
    private BigDecimal walletTotal;

    //客户币数
    @Excel(name = "客户币数")
    private BigDecimal customerTotal;

    //公司币数
    @Excel(name = "平台资产实际币数")
    private BigDecimal companyTotal;

    @Excel(name = "外购币进入数")
    private BigDecimal outerInPlatTotal;

    @Excel(name = "平台提出数")
    private BigDecimal platWithdrawTotal;

}
