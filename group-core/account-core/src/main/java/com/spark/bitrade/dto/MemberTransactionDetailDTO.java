package com.spark.bitrade.dto;

import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.TransactionType;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

/**
 * 交易明细/手续费管理dto
 * @author Zhang Yanjun
 * @time 2018.08.30 09:26
 */
@Data
@ExcelSheet
public class MemberTransactionDetailDTO {
    @Excel(name = "会员昵称")
    private String memberUsername;

//    @Excel(name = "交易记录编号")
    private Long id;

    /**
     * 交易类型
     */
    @Excel(name = "交易类型")
    private String typeOut;
    @Enumerated(EnumType.ORDINAL)
    private TransactionType type;//交易类型

    /**
     * 交易金额
     */
    @Excel(name = "数量")
    private BigDecimal amount;

    /**
     * 币种名称，如 BTC
     */
    @Excel(name = "币种")
    private String symbol;

    /**
     * 交易手续费
     */
    @Excel(name = "手续费")
    private BigDecimal fee;

    /**
     * 创建时间
     */
    @Excel(name = "交易时间")
    private String createTime;

    /**
     * 备注
     */
    @Excel(name = "备注")
    private String comment;

    //手续费抵扣币种单位（不包括当前币种）
    private String feeDiscountCoinUnit;
    //抵扣币种对应手续费
    private BigDecimal feeDiscountAmount;
}
