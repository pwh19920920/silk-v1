package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 中奖记录--状态
 * @author yangch
 * @time 2018.09.13 9:39
 */

@AllArgsConstructor
@Getter
public enum RewardStatus implements BaseEnum {
    UNRECEIVE(0, "待领取"),
    RECEIVED(1, "已领取"),
    INVALID(2,"过期/失效"),
    UNPRIZE(3,"未中奖"),
    PRIZE(4,"中奖");

    //状态,待领取、已领取、过期/失效

    @Setter
    private int code;

    @Setter
    private String nameCn;

    @Override
    @JsonValue
    public int getOrdinal() {
        return code;
    }
}
