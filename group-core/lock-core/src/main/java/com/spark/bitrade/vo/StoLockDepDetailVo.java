package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 部门锁仓记录
 * @author Zhang Yanjun
 * @time 2018.12.04 10:34
 */
@ApiModel
@Data
public class StoLockDepDetailVo {
    @ApiModelProperty(name = "memberId",value = "会员id",dataType = "Long")
    private Long memberId;

    @ApiModelProperty(name = "id",value = "直接部门id",dataType = "Long")
    private Long id;

    @ApiModelProperty(name = "lockType",value = "锁仓类型 0直接锁仓 1团队锁仓 2团队锁仓",dataType = "String")
    private String lockType;

    @ApiModelProperty(name = "amount",value = "锁仓数量")
    private BigDecimal amount;

    @ApiModelProperty(name = "period",value = "周期")
    private Integer period;

    @ApiModelProperty(name = "flag",value = "数量系数")
    private BigDecimal flag;

    @ApiModelProperty(name = "lockTime",value = "锁仓时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lockTime;
}
