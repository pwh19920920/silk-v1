package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author fumy
 * @time 2018.12.04 13:43
 */
@ApiModel
@Data
public class StoLockIncomeVo {

    @ApiModelProperty(name = "rewardType",value = "奖励类型")
    private int rewardType;

    @ApiModelProperty(name = "rewardAmount",value = "奖励总数")
    private BigDecimal rewardAmount;

    @ApiModelProperty(name = "singlePeriodAmount",value = "单期奖励数量")
    private BigDecimal singlePeriodAmount;

    @ApiModelProperty(name = "incomePeriod",value = "分期数")
    private int incomePeriod;

    @ApiModelProperty(name = "currentPeriod",value = "当前奖励期数")
    private int currentPeriod;

    @ApiModelProperty(name = "depId",value = "关联部门Id(memberId)")
    private Long depId;

    @ApiModelProperty(name = "lockTime",value = "锁仓时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lockTime;

    @ApiModelProperty(name = "backTime",value = "到账时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date backTime;

    @ApiModelProperty(name = "daysOfPeroid",value = "每期天数",dataType = "int")
    private int daysOfPeroid;

}
