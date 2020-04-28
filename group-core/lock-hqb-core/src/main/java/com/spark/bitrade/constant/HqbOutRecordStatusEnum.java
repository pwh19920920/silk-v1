package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;

import java.io.Serializable;

public enum HqbOutRecordStatusEnum implements IEnum {
    UNCOMPLETED(0, "未完成"),
    COMPLETED(1, "已完成");

    private final int value;
    private final String desc;

    HqbOutRecordStatusEnum(final int value, final String desc) {
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
