package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public enum CoinFeeType implements BaseEnum,IEnum {


    FIXED("固定"),

    SCALE("比例");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
    @Override
    public Serializable getValue() {
        return this.ordinal();
    }
}
