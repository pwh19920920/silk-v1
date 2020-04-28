package com.spark.bitrade.vo;

import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员余额导出VO
 * @author Zhang Yanjun
 * @time 2018.08.31 17:48
 */
@Data
@ExcelSheet
public class MemberWalletBalanceVO {
    @Excel(name = "会员ID")
    private Long memberId;

    @Excel(name = "用户名")
    private String username ;

    @Excel(name = "邮箱")
    private String email ;

    @Excel(name = "手机号")
    private String mobilePhone ;

    @Excel(name = "真实姓名")
    private String realName ;

    @Excel(name = "币种名称")
    private String unit ;

    @Excel(name = "钱包地址")
    private String address ;

    @Excel(name = "可用币数")
    private BigDecimal balance ;

    @Excel(name = "冻结币数")
    private BigDecimal frozenBalance ;

    @Excel(name = "总币个数")
    private BigDecimal allBalance ;
}
