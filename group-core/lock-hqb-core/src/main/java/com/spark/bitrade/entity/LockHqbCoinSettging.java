package com.spark.bitrade.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 活期宝活动配置表(LockHqbCoinSettging)实体类
 *
 * @author dengdy
 * @since 2019-04-23 15:34:52
 */
@Data
@TableName("lock_hqb_coin_settging")
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "活期宝活动配置")
public class LockHqbCoinSettging implements Serializable {

    private static final long serialVersionUID = 646542533247479099L;

    /**
     * 活动id
     */
    @ApiModelProperty(value = "活动id", name = "activityId")
    private String activityId;

    /**
     * 活动名称
     */
    @ApiModelProperty(value = "活动名称", name = "acitivityName")
    private String acitivityName;
    /**
     * 活动币种
     */
    @ApiModelProperty(value = "活动币种", name = "coinSymbol")
    private String coinSymbol;

    /**
     * 活动计划数量
     */
    @ApiModelProperty(value = "活动计划数量", name = "planAmount")
    private BigDecimal planAmount;

    /**
     * 活动开始时间
     */
    @ApiModelProperty(value = "活动开始时间", name = "startTime")
    private Long startTime;

    /**
     * 活动生效时间
     */
    @ApiModelProperty(value = "活动生效时间", name = "effectTime")
    private Long effectTime;

    /**
     * 活动状态(0:未生效,1:已生效,2:已失效)
     */
    @ApiModelProperty(value = "活动状态(0:未生效,1:已生效,2:已失效)", name = "status")
    private Long status;

    /**
     * 活动备注
     */
    @ApiModelProperty(value = "活动备注", name = "comment")
    private String comment;

    /**
     * 应用或渠道ID
     */
    @ApiModelProperty(value = "应用或渠道ID", name = "appId")
    private String appId;

    /**
     * 币种精度（默认为8位）
     */
    @ApiModelProperty(value = "币种精度（默认为8位）", name = "accuracy")
    private Integer accuracy;

    /**
     * 活动有效时间
     */
    @ApiModelProperty(value = "活动有效时间", name = "validTime")
    private Long validTime;

    /**
     * 日利率，即日万份收益
     */
    @ApiModelProperty(value = "日利率，即日万份收益", name = "dayRate")
    private BigDecimal dayRate;

    /**
     *  更新时间
     */
    @ApiModelProperty(value = "更新时间", name = "updateTime")
    private Date updateTime;

    /**
     *  创建时间
     */
    @ApiModelProperty(value = "更新时间", name = "updateTime")
    private Date createTime;


}