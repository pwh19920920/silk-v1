package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 锁仓活动类型
 * @author yangch
 * @time 2018.06.12 14:39
 */
@AllArgsConstructor
@Getter
public enum LockCoinActivitieType implements BaseEnum {

    /**
     * 整存整取活动
     */
    FIXED_DEPOSIT("整存整取"),
    /**
     * 锁币活动
     */
    COIN_REWARD("锁币");//edit by tansitao 时间： 2018/6/20 原因：调整顺序

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
