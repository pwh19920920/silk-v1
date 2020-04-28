package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 疯狂比特投票统计Vo
 * @author Zhang Yanjun
 * @time 2018.09.17 10:15
 */
@Data
@ExcelSheet
public class VoteStatisticsVo {
    @Excel(name = "序号")
    private String id;//序号
    @Excel(name = "游戏期数")
    private String period;//游戏期数
    @Excel(name = "游戏名称")
    private String name;//游戏名称
    @Excel(name = "开奖时间")
    private String openTime;//开奖时间
    @Excel(name = "游戏参与总人数")
    private long countAll;//游戏参与总人数
    @Excel(name = "参与的新用户数")
    private long countNew;//参与的新用户数
    @Excel(name = "投票总次数")
    private long countVote;//投票总次数
    @Excel(name = "新用户投票总次数")
    private long countNewVote;//新用户投票总次数
    @Excel(name = "投票总额")
    private BigDecimal sumAll;//投票总额
    @Excel(name = "新用户投票总额")
    private BigDecimal sumNewVote;//新用户投票总额
    @Excel(name = "投票币种")
    private String voteSymbol;//投票币种

}
