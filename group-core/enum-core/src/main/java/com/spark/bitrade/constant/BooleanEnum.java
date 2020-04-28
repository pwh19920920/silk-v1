package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Zhang Jinwei
 * @date 2018年01月10日
 */
@AllArgsConstructor
@Getter
public enum BooleanEnum implements BaseEnum , IEnum{
    IS_FALSE(false, "否"),
    IS_TRUE(true, "是");

    @Setter
    private boolean is;

    @Setter
    private String nameCn;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }

    @Override
    public Serializable getValue() {
        return this.ordinal();
    }
}
