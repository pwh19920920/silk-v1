package com.spark.bitrade.dto;

import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员交易记录dto
 * @author tansitao
 * @time 2018/8/23 17:29 
 */
@Data
@ExcelSheet
public class MemberTransactionDTO {
    @Excel(name = "交易记录编号")
    private Long id;

    /**
     * 交易金额
     */
    @Excel(name = "数量")
    private BigDecimal amount;

    /**
     * 创建时间
     */
    @Excel(name = "交易时间")
    private String createTime;

    /**
     * 交易类型
     */
    @Excel(name = "交易类型")
//    @Enumerated(EnumType.ORDINAL)
    private String type;
    /**
     * 币种名称，如 BTC
     */
    @Excel(name = "币种")
    private String symbol;

    /**
     * 交易手续费
     * 提现和转账才有手续费，充值没有;如果是法币交易，只收发布广告的那一方的手续费
     */
    @Excel(name = "手续费")
    private BigDecimal fee;

    /**
     * 备注，目前用于人工充值
     */
    @Excel(name = "备注")
    private String comment;

}
