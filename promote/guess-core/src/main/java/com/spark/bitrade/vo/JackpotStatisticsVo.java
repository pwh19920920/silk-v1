package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 疯狂比特奖池统计Vo
 * @author Zhang Yanjun
 * @time 2018.09.17 21:28
 */
@Data
@ExcelSheet
public class JackpotStatisticsVo {
    @Excel(name = "序号")
    private String id;//序号
    @Excel(name = "游戏期数")
    private String period;//游戏期数
    @Excel(name = "游戏名称")
    private String name;//游戏名称
    @Excel(name = "开奖时间")
    private String openTime;//开奖时间

    @Excel(name = "奖励币种")
    private String prizeSymbol;//奖励币种
    @Excel(name = "红包币种")
    private String redpacketSymbol;//红包币种
    private BigDecimal betNum;//投注总额
    @Excel(name = "投注币种")
    private String betSymbol;//投注币种
    private BigDecimal rewardNum;//已被领取的奖励总额
//    private BigDecimal redpacketNum;//已被领取的红包总额
    private BigDecimal rebateRatio;//返佣比例
    private BigDecimal prizeRatio;//奖励比例
    private BigDecimal backRatio;//回购比例
    private BigDecimal redpacketRatio;//红包比例
    private int redpacketState;//是否开启红包  0否  1是
    private BigDecimal countPromote;//返给用户的推荐返佣总额(未乘以返佣比例)  需返佣的投注总额

    @Excel(name = "奖池累计")
    private BigDecimal jackpotAll;//奖池累计
    @Excel(name = "推荐分红总额")
    private BigDecimal promotion;//推荐分红总额
    @Excel(name = "推荐分红剩余")
    private BigDecimal promotionBalance;//推荐分红剩余
    @Excel(name = "奖金发放总额")
    private BigDecimal reward;//总奖金发放
    @Excel(name = "奖金剩余")
    private BigDecimal rewardBalance;//奖金发放剩余
    @Excel(name = "红包活动总额")
    private BigDecimal redpacket;//红包活动
    @Excel(name = "红包剩余")
    private BigDecimal redpacketBalance;//红包剩余
    @Excel(name = "SLU回购")
    private BigDecimal sluBack;//SLU回购
    private BigDecimal jackpotBalance;//奖池余量
    @Excel(name = "下期沉淀")
    private BigDecimal deposit;//下期沉淀
}
