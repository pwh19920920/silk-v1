package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;

import java.io.Serializable;

public enum HqbInRecordStatusEnum implements IEnum {
    UNCONFIRMED(0, "未确认"),
    CONFIRMED(1, "已确认"),
    CONFIRMED_Failed(2, "确认失败");

    private final int value;
    private final String desc;

    HqbInRecordStatusEnum(final int value, final String desc) {
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
