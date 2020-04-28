package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 投注记录的状态
 * @author yangch
 * @time 2018.09.14 10:43
 */

@AllArgsConstructor
@Getter
public enum BettingRecordStatus implements BaseEnum {
    WAITING(0, "待开奖"),
    LOST(1, "未中奖"),
    PRAISE(2,"中奖");

    //枚举，待开奖、未中奖,中奖

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
