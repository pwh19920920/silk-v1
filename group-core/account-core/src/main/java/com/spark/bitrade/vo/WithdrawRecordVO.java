package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.WithdrawStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ExcelSheet
public class WithdrawRecordVO {

    private Long id ;
    @Excel(name="用户id")
    private Long memberId ;
    @Excel(name="用户名")
    private String memberUsername ;
    @Excel(name="真实姓名")
    private String memberRealName ;
    @Excel(name="币种")
    private String unit ;
    @Excel(name="提币数量")
    private BigDecimal totalAmount ;
    @Excel(name="提币手续费")
    private BigDecimal fee ;
    @Excel(name="实际到账数量")
    private BigDecimal arrivedAmount ;
    @Excel(name="交易流水号")
    private String transactionNumber ;

    @Excel(name="操作时间")
    private String createTimeOut;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;//操作时间

    @Excel(name="提币地址")
    private String address ;

    @Excel(name="状态")
    private String statusOut;
    private WithdrawStatus status ;//状态

    @Excel(name="备注")
    private String remark ;

    @Excel(name="是否自动提币")
    private String isAutoOut;
    private BooleanEnum isAuto ;//是否自动提币

//    @Excel(name="错误原因")
    //add by shenzucai 时间： 2018.05.25 原因：后台添加失败原因
    private String errorRemark;

    /**
     * 抵扣币种
     */
    @Excel(name="手续费抵扣币种单位（不包括当前币种）")
    private String feeDiscountCoinUnit;
    /**
     * 抵扣数量
     */
    @Excel(name="抵扣币种对应手续费")
    private BigDecimal feeDiscountAmount;

    /**
     * 备注
     */
    @Excel(name="用于手续费抵扣备注")
    private String comment;

}
