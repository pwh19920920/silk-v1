package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 三方平台实体
 * @author fumy
 * @time 2018.08.03 10:51
 */
@ApiModel
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThirdPlatform {

    @ApiModelProperty(value = "id",name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 平台申请秘钥
     */
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
    @ApiModelProperty(value = "启用状态，0：否，1：是",name = "status")
    private BooleanEnum status;

    /**
     * 是否允许签约币种和入账币种一致，不一致则支付时需要进行币种价值转换
     */
    @ApiModelProperty(value = "是否允许签约币种和入账币种一致，0：否，1：是",name = "coinCheck")
    private BooleanEnum coinCheck;

    /**
     * 折扣率
     */
    @ApiModelProperty(value = "折扣率",name = "discount")
    private BigDecimal discount;
}
