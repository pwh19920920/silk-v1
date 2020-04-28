package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 疯狂比特中奖统计Vo
 * @author Zhang Yanjun
 * @time 2018.09.17 10:58
 */
@Data
@ExcelSheet
public class RewardStatisticsVo {
    @Excel(name = "序号")
    private String id;//序号
    @Excel(name = "游戏期数")
    private String period;//游戏期数
    @Excel(name = "游戏名称")
    private String name;//游戏名称
    @Excel(name = "开奖时间")
    private String openTime;//开奖时间
    @Excel(name = "新用户中奖人数")
    private long newCount;//新用户中奖人数
    @Excel(name = "新用户中奖总金额")
    private BigDecimal newSum;//新用户中奖总金额
    @Excel(name = "老用户中奖人数")
    private long oldCount;//老用户中奖人数
    @Excel(name = "老用户中奖总金额")
    private BigDecimal oldSum;//老用户中奖总金额
    @Excel(name = "奖励币种")
    private String rewardSymbol;//奖励币种
}
