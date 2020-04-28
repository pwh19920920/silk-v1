package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.LockBccAssignUnlockTypeEnum;
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
 * (LockBccAssignUnlock)实体类
 *
 * @author fatKarin
 * @since 2019-06-17 16:01:14
 */

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "BCC赋能佣金解锁记录")
public class LockBccAssignUnlock {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @ApiModelProperty(name = "id",value = "id")
    private Long id;

    @ApiModelProperty(name = "activityId",value = "活动id")
    private Long activityId;

    @ApiModelProperty(name = "memberId",value = "会员id")
    private Long memberId;

    @ApiModelProperty(name = "releaseType",value = "释放类型{0:佣金释放,1:ieo锁仓}")
    private LockBccAssignUnlockTypeEnum releaseType;

    @ApiModelProperty(name = "releasedAmount",value = "释放数量")
    private BigDecimal releasedAmount;

    @ApiModelProperty(name = "comment",value = "备注")
    private String comment;

    @ApiModelProperty(name = "releaseTime",value = "释放时间")
    private Date releaseTime;

}