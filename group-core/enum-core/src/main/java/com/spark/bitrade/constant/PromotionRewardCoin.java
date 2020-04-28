package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 返佣结算币种（交易币、USDT、SLB）

 * @author yangch
 * @time 2018.05.29 19:02
 */
@AllArgsConstructor
@Getter
public enum PromotionRewardCoin implements BaseEnum {
    /**
     * 使用交易币结算
     */
    REWARDSOURCECOIN("交易币"),
    /**
     * 使用USDT结算
     */
    REWARDUSDT("USDT"),
    /**
     * 使用SLB结算
     */
    REWARDSLB("SLB") ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
