package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 返佣周期类型（实时、天、周、月）

 * @author yangch
 * @time 2018.05.29 17:24
 */
@AllArgsConstructor
@Getter
public enum PromotionRewardCycle implements BaseEnum {
    /**
     * 实时
     */
    REALTIME("实时"),
    /**
     * 天
     */
    DAILY("天"),
    /**
     * 币币交易
     */
    WEEKLY("周"),
    /**
     * 月
     */
    MONTHLY("月");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
