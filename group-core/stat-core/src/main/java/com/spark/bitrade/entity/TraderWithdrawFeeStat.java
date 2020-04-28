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
 * 提币手续费统计表
 * </p>
 *
 * @author fumy
 * @since 2018-06-14
 */
@Entity
@Data
@ExcelSheet
public class TraderWithdrawFeeStat{

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
    @Excel(name = "币种单位")
    private String unit;
    /**
     * 总提币数
     */
    @Excel(name = "总提币数")
    @Column(columnDefinition = "decimal(19,8) comment '总提币数'")
    private BigDecimal withdrawTotal;
    /**
     * 实际到账数
     */
    @Excel(name = "实际到账数")
    @Column(columnDefinition = "decimal(19,8) comment '实际到账数'")
    private BigDecimal actualArrivedTotal;
    /**
     * 总提币手续费
     */
    @Excel(name = "总提币手续费")
    @Column(columnDefinition = "decimal(19,8) comment '总提币手续费'")
    private BigDecimal withdrawFeeTotal;

}
