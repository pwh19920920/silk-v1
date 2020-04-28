package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 推荐级别
 */
@AllArgsConstructor
@Getter
public enum RewardRecordLevel implements BaseEnum {
    /**
     * 1级
     */
    ONE("1级"),
    /**
     * 2级
     */
    TWO("2级"),
    /**
     * 3级
     */
    THREE("3级");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

}
