package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.IncomeType;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.SettlementType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 锁仓记录
 * @author tansitao
 * @time 2018/6/20 15:52 
 */
@Data
@ApiModel(description = "锁仓记录返回实体")
public class LockCoinDetailBuilder {
    @ApiModelProperty(value = "id",name = "id")
    private long id;

    @ApiModelProperty(value = "币种",name = "symbol")
    private String symbol;

    @ApiModelProperty(value = "锁仓份数",name = "lockCopies")
    private BigDecimal lockCopies;

    @ApiModelProperty(value = "锁仓数量",name = "lockNum")
    private BigDecimal lockNum;

    @ApiModelProperty(value = "锁仓时间",name = "lockTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lockTime;

    @ApiModelProperty(value = "周期",name = "cycle")
    private double cycle;

    @ApiModelProperty(value = "年化率",name = "earningRate")
    private BigDecimal earningRate;

    @ApiModelProperty(value = "计划解锁时间，为null，表示解锁时间未知",name = "planUnlockTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date planUnlockTime;

    @ApiModelProperty(value = "预期收益",name = "planIncome")
    private BigDecimal planIncome;

    @ApiModelProperty(value = "状态",name = "status")
    private LockStatus status;

    @ApiModelProperty(value = "解锁时间",name = "unlockTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date unlockTime;

    @ApiModelProperty(value = "撤销时间",name = "cancleTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancleTime;

    @ApiModelProperty(value = "锁仓价格（USDT）",name = "lockPrice")
    private BigDecimal lockPrice;

    @ApiModelProperty(value = "大活动Id",name = "refActivitieId")
    private long refActivitieId;

    @ApiModelProperty(value = "锁仓总金额",name = "totalCNY")
    private BigDecimal totalCNY;

    @ApiModelProperty(value = "锁仓时usdt价格",name = "usdtPriceCNY")
    private BigDecimal usdtPriceCNY;

    @ApiModelProperty(value = "解锁时时价格(人名币)",name = "unLockPriceCNY")
    private BigDecimal unLockPriceCNY;

    @ApiModelProperty(value = "解锁时usdt的人民币价格",name = "unLockUSDTPriceCNY")
    private BigDecimal unLockUSDTPriceCNY;

    @ApiModelProperty(value = "解锁违约金",name = "damagesAmount")
    private BigDecimal damagesAmount;

    @ApiModelProperty(value = "锁仓收益金额",name = "lockIncomeCNY")
    private BigDecimal lockIncomeCNY;

    @ApiModelProperty(value = "结算类型",name = "settlementType")
    private SettlementType settlementType;

    @ApiModelProperty(value = "实际结算收益",name = "settlementIncome")
    private BigDecimal settlementIncome;

    @ApiModelProperty(value = "收益类型",name = "incomeType")
    private IncomeType incomeType;

    @ApiModelProperty(value = "是否过期（0否1是）",name = "isOverdue")
    private BooleanEnum isOverdue;

    @ApiModelProperty(value = "锁仓期数",name = "lockCycle")
    private Integer lockCycle;

    @ApiModelProperty(value = "开始释放",name = "beginDays")
    private Integer beginDays;

    @ApiModelProperty(value = "每期天数",name = "cycleDays")
    private Integer cycleDays;

    @ApiModelProperty(value = "周期比例",name = "cycleRatio")
    private String cycleRatio;


}
