package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 *  
 *   商家报价VO 
 *  @author liaoqinghui  
 *  @time 2019.07.12 13:35  
 */
@Data
@ApiModel
public class MQutoesVo {

    //商家广告id
    @ApiModelProperty(value = "广告ID",name = "advertiseId",example = "11111")
    private Long advertiseId;
    //单价
    @ApiModelProperty(value = "币种单价",name = "singlePrice",example = "11111.11")
    private BigDecimal singlePrice;
    //总价
    @ApiModelProperty(value = "总价格",name = "totalPrice",example = "11111.00")
    private BigDecimal totalPrice;

}
