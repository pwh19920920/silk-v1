package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class AnnualRateOfWeekVO {

    /**
     * 当前日期七日年化率
     */
    @ApiModelProperty(name = "annualRateOfWeek",value = "当前日期七日年化率")
    private BigDecimal annualRateOfWeek;

    /**
     * 日期 YY-MM-DD 格式
     */
    @ApiModelProperty(name = "date",value = "日期 YY-MM-DD 格式")
    private String date;

    /**
     * 当前日期收益
     */
    @ApiModelProperty(name = "income",value = "当前日期收益")
    private BigDecimal income;
}
