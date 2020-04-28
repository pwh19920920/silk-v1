package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 锁仓活动
 * @author tansitao
 * @time 2018/6/20 15:52 
 */
@Builder
@Data
@ApiModel(description = "锁仓活动信息实体")
public class LockActivitySettingBuilder {
    @ApiModelProperty(value = "id",name = "id")
    private long id;

    @ApiModelProperty(value = "活动名称",name = "name")
    private String name;

    @ApiModelProperty(value = "活动名称",name = "note")
    private String note;

    @ApiModelProperty(value = "支付币种",name = "baseSymbol")
    private String baseSymbol;

    @ApiModelProperty(value = "活动币种",name = "symbol")
    private String symbol;

    @ApiModelProperty(value = "活动每份数量（1表示1个币，大于1表示每份多少币）",name = "unitPerAmount")
    private BigDecimal unitPerAmount;

    @ApiModelProperty(value = "活动计划数量（币数、份数）",name = "planAmount")
    private BigDecimal planAmount;

    @ApiModelProperty(value = "活动参与数量（币数、份数）",name = "boughtAmount")
    private BigDecimal boughtAmount;

    @ApiModelProperty(value = "最低购买数量（币数、份数）",name = "minBuyAmount")

    private BigDecimal minBuyAmount;
    @ApiModelProperty(value = "最大购买数量（币数、份数）",name = "maxBuyAmount")
    private BigDecimal maxBuyAmount;

    @ApiModelProperty(value = "锁仓天数",name = "lockDays")
    private Integer lockDays;

    @ApiModelProperty(value = "开始释放",name = "beginDays")
    private Integer beginDays;

    @ApiModelProperty(value = "每期天数",name = "cycleDays")
    private Integer cycleDays;

    @ApiModelProperty(value = "周期比例",name = "cycleRatio")
    private String cycleRatio;

    @ApiModelProperty(value = "预期收益",name = "planIncome")
    private BigDecimal planIncome;

    @ApiModelProperty(value = "年化率",name = "earningRate")
    private BigDecimal earningRate;

    @ApiModelProperty(value = "周期",name = "cycle")
    private double cycle;

    @ApiModelProperty(value = "活动开始时间",name = "startTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "活动截止时间",name = "endTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @ApiModelProperty(value = "最大购买份数(最大购买量除以每份数量)",name = "maxPurchase")
    private Integer maxPurchase;

    @ApiModelProperty(value = "活动状态(是否过期,0未过期,1已过期,2未开始)",name = "isOverdue")
    private Integer isOverdue;

    @ApiModelProperty(value = "当前时间",name = "nowTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nowTime;

    @ApiModelProperty(value = "标题图片",name = "imgUrl")
    private String imgUrl;
}
