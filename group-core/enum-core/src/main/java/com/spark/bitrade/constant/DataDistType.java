package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Zhang Yanjun
 * @time 2019.02.27 11:12
 */
@AllArgsConstructor
@Getter
public enum DataDistType implements BaseEnum,IEnum{
    java_lang_String("String", String.class),
    java_lang_Byte("Byte", Byte.class),
    java_lang_Short("Short", Short.class),
    java_lang_Integer("Integer", Integer.class),
    java_lang_Long("Long", Long.class),
    java_lang_Double("Double", Double.class),
    java_math_BigDecimal("BigDecimal", BigDecimal.class),
    java_lang_Float("Float", Float.class),
    java_lang_Boolean("Boolean", Boolean.class),
    java_lang_Character("Char", Character.class);

    @Setter
    private String cnName;
    @Setter
    public Class<?> clazz;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

    @Override
    public Serializable getValue() {
        return this.name();
    }

//    DataDistType(String cnName, Class<?> clazz){
//        this.cnName = cnName;
//        this.clazz = clazz;
//    }
}
