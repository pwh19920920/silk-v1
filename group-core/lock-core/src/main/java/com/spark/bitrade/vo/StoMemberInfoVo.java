package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户的级别和佣金奖励信息
 * @author Zhang Yanjun
 * @time 2018.12.04 11:25
 */
@ApiModel
@Data
public class StoMemberInfoVo {

    @ApiModelProperty(name = "memberId",value = "会员id",dataType = "Long")
    private Long memberId;

    @ApiModelProperty(name = "position",value = "职务",dataType = "String")
    private String position;

    @ApiModelProperty(name = "referrerAmount",value = "销售奖总金额",dataType = "String")
    private BigDecimal referrerAmount;

    @ApiModelProperty(name = "crossAmount",value = "管理奖总金额",dataType = "String")
    private BigDecimal crossAmount;

    @ApiModelProperty(name = "trainingAmount",value = "培育奖总金额",dataType = "String")
    private BigDecimal trainingAmount;

    @ApiModelProperty(name = "referrerAmount",value = "销售奖到账金额",dataType = "String")
    private BigDecimal referrerArrived;

    @ApiModelProperty(name = "crossAmount",value = "管理奖到账金额",dataType = "String")
    private BigDecimal crossArrived;

    @ApiModelProperty(name = "trainingAmount",value = "培育奖到账金额",dataType = "String")
    private BigDecimal trainingArrived;
}
