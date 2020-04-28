package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 活期宝首页综合信息
 *
 * @author Zhang Yanjun
 * @time 2019.04.24 17:01
 */
@Data
@ApiModel(description = "活期宝首页综合信息")
public class MemberGeneralInfoVO {

    /**
     * 活动名称
     */
    @ApiModelProperty(name = "acitivityName", value = "活动名称")
    private String acitivityName;

    /**
     * 活动状态(0:未生效,1:已生效,2:已失效)
     */
    @ApiModelProperty(value = "活动状态(0:未生效,1:已生效,2:已失效)", name = "status")
    private int status;

    /**
     * 币种
     */
    @ApiModelProperty(name = "unit", value = "币种")
    private String unit;

    /**
     * 活期宝账户总额
     */
    @ApiModelProperty(name = "amount",value = "活期宝账户总额")
    private BigDecimal amount;
    /**
     * 昨日收益
     */
    @ApiModelProperty(name = "yesterdayIncome",value = "昨日收益")
    private BigDecimal yesterdayIncome;

    /**
     * 累计收益
     */
    @ApiModelProperty(name = "accumulateIncome",value = "累计收益")
    private BigDecimal accumulateIncome;

    /**
     * 万份收益
     */
    @ApiModelProperty(name = "thousandsIncome",value = "万份收益")
    private BigDecimal thousandsIncome;

    /**
     * 7日年化率
     */
    @ApiModelProperty(name = "annualRateOfWeek",value = "7日年化率")
    private BigDecimal annualRateOfWeek;

}
