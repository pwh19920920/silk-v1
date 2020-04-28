package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 违约金计算类型
 * @author tansitao
 * @time 2018/7/3 12:01 
 */
@AllArgsConstructor
@Getter
public enum DamagesCalcType implements BaseEnum {

    PERCENT("百分比"),//0

    FIXED_NUMBER("固定数量")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
