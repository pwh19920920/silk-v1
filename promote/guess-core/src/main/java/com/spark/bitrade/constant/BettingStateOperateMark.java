package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 任务处理标记
 * @author yangch
 * @time 2018.09.13 9:39
 */

@AllArgsConstructor
@Getter
public enum BettingStateOperateMark implements BaseEnum {
    UNTREATED(0, "未处理"),
    TREATING(1, "处理中"),
    TREATED(2,"已处理");

    //标记(未处理,处理中,已处理)

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
