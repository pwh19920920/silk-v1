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
 * 运行审核(OperationAudit)表实体类
 *
 * @author daring5920
 * @since 2019-09-04 10:51:47
 */
@SuppressWarnings("serial")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "运行审核")
public class OperationAudit {

    /**
     * 财务审核id
     */
    @TableId
    @ApiModelProperty(value = "财务审核id", example = "")
    private Long id;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", example = "")
    private String userName;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id", example = "")
    private Long memberId;

    /**
     * 交易id
     */
    @ApiModelProperty(value = "交易id", example = "")
    private String transactionNumber;

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
     * 数量
     */
    @ApiModelProperty(value = "数量", example = "")
    private BigDecimal amount;

    /**
     * 申请时间
     */
    @ApiModelProperty(value = "申请时间", example = "")
    private Date applyTime;

    /**
     * 0 未审核，1审核通过，2审核拒绝 默认 0
     */
    @ApiModelProperty(value = "0 未审核，1审核通过，2审核拒绝 默认 0", example = "")
    private Integer auditStatus;

    /**
     * 审核
     */
    @ApiModelProperty(value = "审核", example = "")
    private String auditUserId;

    /**
     * 审核备注
     */
    @ApiModelProperty(value = "审核备注", example = "")
    private String comment;

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