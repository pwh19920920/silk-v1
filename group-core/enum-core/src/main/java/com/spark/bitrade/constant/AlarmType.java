package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 告警类型
 * @author Zhang Yanjun
 * @time 2018.09.27 10:25
 */
@AllArgsConstructor
@Getter
public enum AlarmType implements BaseEnum {
    UNKNOWN(0,"未知错误"),
    CANCEL(1,"币币交易撤单"),
    OTC_APPEAL(2,"C2C申诉"),
    OTC_CANCEL(3,"C2C撤单");

    @Setter
    private int code;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
