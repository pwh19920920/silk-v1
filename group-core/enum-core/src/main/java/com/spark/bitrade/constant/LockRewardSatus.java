package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 返佣类型
 * @author fumy
 * @time 2018.07.24 15:49
 */
@AllArgsConstructor
@Getter
public enum LockRewardSatus implements BaseEnum{

    /**
     * 默认，不返佣
     */
    DEFAULT_REWARD("不返佣"),
    /**
     * 未返佣
     */
    NO_REWARD("未返佣"),
    /**
     * 已返佣
     */
    ALREADY_REWARD("已返佣")
    ;


    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
