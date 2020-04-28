package com.spark.bitrade.dto;

import com.spark.bitrade.entity.LockCoinRechargeSetting;
import lombok.Data;

import java.math.BigDecimal;

/***
 * 
 * @author yangch
 * @time 2018.08.02 10:44
 */
@Data
public class LockCoinRechargeSettingDto extends LockCoinRechargeSetting {
    //上一次的解锁价格
    private BigDecimal prevUnlockPrice = BigDecimal.ZERO;
}
