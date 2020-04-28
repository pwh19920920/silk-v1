package com.spark.bitrade.dto;

import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author fumy
 * @time 2018.10.24 09:52
 */
@ApiModel
@Data
public class PayThirdPlatDto {


    @ApiModelProperty(value = "id",name = "id")
    private Long id;


    @ApiModelProperty(value = "平台申请秘钥",name = "platformKey")
    private String platformKey;

    /**
     * 平台名称
     */
    @ApiModelProperty(value = "平台名称",name = "platformName")
    private String platformName;

    /**
     * 启用状态
     */
    @ApiModelProperty(value = "启用状态",name = "status")
    private BooleanEnum status;

    /**
     * 是否允许签约币种和入账币种一致，不一致则支付时需要进行币种价值转换
     */
    @ApiModelProperty(value = "是否允许签约币种和入账币种一致",name = "coinCheck")
    private BooleanEnum coinCheck;

    /**
     * 折扣率
     */
    @ApiModelProperty(value = "折扣率",name = "discount")
    private BigDecimal discount;

}
