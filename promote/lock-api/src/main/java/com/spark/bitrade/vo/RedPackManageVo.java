package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.11.26 09:10  
 */
@Data
public class RedPackManageVo {

    /**
     * 活动id
     */
    @ApiModelProperty(value = "活动id")
    private Long id;

    /**
     * 活动名称
     */
    @ApiModelProperty(value = "活动名称")
    private String redpackName;

    /**
     * 活动币种
     */
    @ApiModelProperty(value = "")
    private String unit;

    /**
     * 红包时限小时
     */
    @ApiModelProperty(value = "红包时限小时")
    private Integer within;

    /**
     * 限用户类型参与活动{0:所有,1:新会员, 2:老会员}
     */
    @ApiModelProperty(value = "限用户类型参与活动{0:所有,1:新会员, 2:老会员}")
    private Integer isOldUser;

    /**
     * 首页弹出优先级
     */
    @ApiModelProperty(value = "首页弹出优先级")
    private Integer priority;

//
//    @ApiModelProperty(value = "图片地址？")
//    private String url;

    /**
     * 剩余数量
     */
    private BigDecimal redPacketBalance;

}
