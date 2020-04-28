package com.spark.bitrade.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Administrator on 2018/7/12.
 */
@Data
@ExcelSheet
public class LockTypeDto {
    @Excel(name = "活动名称")
    private String name;
    @Excel(name = "会员昵称")
    private String userName;
    @Excel(name = "id")
    private long id;
    @Excel(name = "会员ID")
    private long memberId;
    @Excel(name = "活动币种")
    private String coinUnit;

    private Long refActivitieId;
    @Excel(name = "总锁仓币数")
    private BigDecimal totalAmount;
    @Excel(name = "锁仓价格相对USDT")
    private  BigDecimal lockPrice;
    @Excel(name = "剩余锁仓币数")
    private BigDecimal remainAmount;
    @Excel(name = "锁仓开始时间")
//    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String lockTime;

    @Excel(name = "计划解锁时间")
//    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String planUnlockTime; //为null，表示解锁时间未知
    @Excel(name = "预计收益")
    private BigDecimal planIncome;
    @Excel(name = "锁仓状态")
    private String lockStatus;
    private LockStatus status;//锁仓状态
    @Excel(name = "解锁时间")
//    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String cancleTime;
    private Integer lockDays; //锁仓时长（单位：天）
    private BigDecimal earningRate; //收益保障：最低年化率
    private BigDecimal unitPerAmount;//活动每份数量（1表示1个币，大于1表示每份多少币）


    private LockType type;//锁仓类型
    @Excel(name = "锁仓类型")
    private String lockType;
}
