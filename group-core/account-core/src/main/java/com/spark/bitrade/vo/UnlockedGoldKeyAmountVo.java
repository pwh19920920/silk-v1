package com.spark.bitrade.vo;

import com.spark.bitrade.constant.TransactionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(description = "金钥匙活动用户解锁金钥匙数记录")
public class UnlockedGoldKeyAmountVo {
    /**
     * 锁仓类型
     */
    @ApiModelProperty(name = "lockType",value = "锁仓类型")
    private TransactionType lockType;

    /**
     * 解锁BTTC金钥匙数
     */
    @ApiModelProperty(name = "unlockedAmount",value = "解锁BTTC金钥匙数")
    private BigDecimal unlockedAmount;

    /**
     * 解锁时间
     */
    @ApiModelProperty(name = "lockTime",value = "解锁时间")
    private Date lockTime;
}
