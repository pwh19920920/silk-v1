package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;

import java.io.Serializable;

public enum HqbOutRecordTypeEnum implements IEnum {
    IMMEDIATELY(0, "立即到账"),
    DELAYED(1, "延时到账");

    private final int value;
    private final String desc;

    HqbOutRecordTypeEnum(final int value, final String desc) {
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
