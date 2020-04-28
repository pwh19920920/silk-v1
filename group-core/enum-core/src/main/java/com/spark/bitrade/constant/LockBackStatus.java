package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 返回状态
 * @author Zhang Yanjun
 * @time 2018.12.03 17:38
 */
@AllArgsConstructor
@Getter
public enum LockBackStatus implements BaseEnum{
    BACK("待返还"),//0

    BACKING("返还中"),//1

    BACKED("已返还"), //2

    BANLANCE_INSUFFICIENT("锁仓余额不足"),//3
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
