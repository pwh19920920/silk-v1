package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 活动数量类型
 * @author tansitao
 * @time 2018/11/20 10:46 
 */
@AllArgsConstructor
@Getter
public enum ActivitieNumType implements BaseEnum {
    none("没有活动"),
    one("一个活动"),
    many("很多活动")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
