package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 中奖记录--业务类型
 * @author yangch
 * @time 2018.09.13 9:39
 */

@AllArgsConstructor
@Getter
public enum RewardBusinessType implements BaseEnum {
    GUESS(0, "竞猜"),
    REDPACKET(1, "抢红包");

    //业务类型,竞猜、抢红包

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
