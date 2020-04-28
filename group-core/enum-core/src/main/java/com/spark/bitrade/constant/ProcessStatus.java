package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 处理状态
 * @author yangch
 * @time 2018.08.02 11:53
 */

@AllArgsConstructor
@Getter
public enum ProcessStatus implements BaseEnum {

    NOT_PROCESSED("未处理"), //0
    PROCESSING("处理中"),     //1
    PROCESSED("已处理");      //2

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
