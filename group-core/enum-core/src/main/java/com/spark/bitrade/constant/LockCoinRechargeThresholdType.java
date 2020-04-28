package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 锁仓充值 阀值、释放类型
 * @author yangch
 * @time 2018.06.12 14:39
 */
@AllArgsConstructor
@Getter
public enum LockCoinRechargeThresholdType implements BaseEnum {
    /**
     * 币价
     */
    COIN_VALUE("币价"),
    /**
     * 涨幅
     */
    COIN_RANGE("涨幅");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
