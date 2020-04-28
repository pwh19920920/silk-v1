package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;
import java.io.Serializable;

/**
 * (LockBccAssignRecord)实体类
 *
 * @author fatKarin
 * @since 2019-06-17 15:40:24
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "bcc赋能锁仓详情实体类")
public class LockBccAssignRecord {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @ApiModelProperty(name = "id",value = "id")
    private Long id;

    @ApiModelProperty(name = "activityId",value = "活动id")
    private Long activityId;

    @ApiModelProperty(name = "memberId",value = "会员id")
    private Long memberId;

    @ApiModelProperty(name = "superiorId",value = "上级用户id")
    private Long superiorId;

    @ApiModelProperty(name = "lockPortion",value = "已参投份数")
    private Integer lockPortion;

    @ApiModelProperty(name = "lockAmount",value = "累计参投金额")
    private BigDecimal lockAmount;

    @ApiModelProperty(name = "commissionReward",value = "赠送佣金")
    private BigDecimal commissionReward;

    @ApiModelProperty(name = "rewardPortion",value = "累计赠送份数")
    private Integer rewardPortion;

    @ApiModelProperty(name = "releasePortion",value = "赠送待释份数")
    private Integer releasePortion;

    @ApiModelProperty(name = "rewardAmount",value = "累计赠送数量")
    private BigDecimal rewardAmount;

    @ApiModelProperty(name = "releaseAmount",value = "赠送待释数量")
    private BigDecimal releaseAmount;

    @ApiModelProperty(name = "status",value = "状态{0:失效,1:生效}")
    private BooleanEnum status;

    @ApiModelProperty(name = "comment",value = "备注")
    private String comment;

    @ApiModelProperty(name = "createTime",value = "参投时间")
    private Date createTime;

    @ApiModelProperty(name = "updateTime",value = "更新时间")
    private Date updateTime;

}