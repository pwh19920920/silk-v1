package com.spark.bitrade.entity;

import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotations.TableId;
import lombok.Data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 退币记录(BackCoinRecord)表实体类
 *
 * @author daring5920
 * @since 2019-09-04 10:51:08
 */
@SuppressWarnings("serial")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "退币记录")
public class BackCoinRecord {

    /**
     * 财务审核id
     */
    @TableId
    @ApiModelProperty(value = "财务审核id", example = "")
    private Long id;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id", example = "")
    private Long memberId;

    /**
     * 充值交易hash值
     */
    @ApiModelProperty(value = "充值交易hash值", example = "")
    private String rechargeTransactionNumber;

    /**
     * 交易id
     */
    @ApiModelProperty(value = "交易id", example = "")
    private String transactionNumber;

    /**
     * 平台地址
     */
    @ApiModelProperty(value = "平台地址", example = "")
    private String platformAddress;

    /**
     * 币种单位
     */
    @ApiModelProperty(value = "币种单位", example = "")
    private String unit;

    /**
     * 地址
     */
    @ApiModelProperty(value = "地址", example = "")
    private String address;

    /**
     * 手续费
     */
    @ApiModelProperty(value = "手续费", example = "")
    private BigDecimal fee;

    /**
     * 数量
     */
    @ApiModelProperty(value = "数量", example = "")
    private BigDecimal amount;

    /**
     * 0 待退还，1已退还，2退还失败 默认0
     */
    @ApiModelProperty(value = "0 待退还，1已退还，2退还失败 默认0", example = "")
    private Integer status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", example = "")
    private Date updateTime;


}