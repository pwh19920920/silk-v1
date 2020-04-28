package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.LockBackStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * (LockIeoRestitutionIncomePlan)实体类
 *
 * @author dengdy
 * @since 2019-04-16 15:07:32
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table
@ApiModel()
public class LockIeoRestitutionIncomePlan{

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id",name = "id")
    private Long id;

    /**
     * 当前一期返还总数
     */
    @ApiModelProperty(value = "当前一期返还总数",name = "restitutionAmount")
    private BigDecimal restitutionAmount;

    /**
     *记录校验码
     */
    @ApiModelProperty(value = "记录校验码",name = "code")
    private String code;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注",name = "comment")
    private String comment;

    /**
     * 关联锁仓详情id
     */
    @ApiModelProperty(value = "关联锁仓详情id",name = "lockDetailId")
    private Long lockDetailId;

    /**
     * 关联会员id
     */
    @ApiModelProperty(value = "关联会员id",name = "memberId")
    private Long memberId;

    /**
     * 返还期数(从1开始)
     */
    @ApiModelProperty(value = "返还期数(从1开始)",name = "period")
    private Integer period;

    /**
     * 返还状态(0:待返还,1:返还中,2:已返还)
     */
    @ApiModelProperty(value = "返还状态(0:待返还,1:返还中,2:已返还)",name = "status")
    @Enumerated(EnumType.ORDINAL)
    private LockBackStatus status;

    /**
     * 返还币种名称
     */
    @ApiModelProperty(value = "返还币种名称",name = "symbol")
    private String symbol;

    /**
     * 返还时间
     */
    @ApiModelProperty(value = "返还时间",name = "rewardTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date rewardTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间",name = "createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间",name = "updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}