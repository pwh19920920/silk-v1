package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "BCC赋能计划锁仓信息")
public class LockBccLockedInfoVo {

    @ApiModelProperty(value = "剩余可锁",name = "admitLockAmount")
    private BigDecimal admitLockAmount;

    @ApiModelProperty(value = "已锁数量",name = "lockedAmount")
    private BigDecimal lockedAmount;

    @ApiModelProperty(value = "待解数量",name = "unlockAmount")
    private BigDecimal unlockAmount;

    @ApiModelProperty(value = "可用余额",name = "balanceAmount")
    private BigDecimal balanceAmount;
}
