package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;

import java.io.Serializable;


public enum LockBccAssignUnlockTypeEnum implements IEnum {
    //
    COMMISSION(0, "佣金释放"),
    IEO(1, "IEO加速释放");

    private final int value;
    private final String cnName;

    LockBccAssignUnlockTypeEnum(final int value, final String cnName) {
        this.value = value;
        this.cnName = cnName;
    }

    @Override
    public Serializable getValue() {
        return this.value;
    }

    // Jackson 注解为 JsonValue 返回中文 json 描述
    public String getCnName() {
        return this.cnName;
    }

}
