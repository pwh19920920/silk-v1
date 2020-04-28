package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;

import java.io.Serializable;

public enum LockMarketExtraRewardConfigStatusEnum implements IEnum {
    DISABLED(0, "失效"),
    NORMAL(1, "有效");

    private final int value;
    private final String desc;

    LockMarketExtraRewardConfigStatusEnum(final int value, final String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Serializable getValue() {
        return this.value;
    }

    // Jackson 注解为 JsonValue 返回中文 json 描述
    public String getDesc() {
        return this.desc;
    }

}
