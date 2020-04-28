package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 金钥匙活动用户账户数据
 * @author dengdy
 * @time 2019/5/8
 */
@Data
@ApiModel(description = "金钥匙活动用户账户数据")
public class LockBttcOfflineWalletVo {
    /**
     * 可用余额
     */
    @ApiModelProperty(name = "balance",value = "可用余额")
    private BigDecimal balance;

    /**
     * 可解金钥匙总数
     */
    @ApiModelProperty(name = "enableUnlockAmount",value = "可解金钥匙总数")
    private BigDecimal enableUnlockAmount;

    /**
     * 已解金钥匙总数
     */
    @ApiModelProperty(name = "unlockedAmount",value = "已解金钥匙总数")
    private BigDecimal unlockedAmount;
}
