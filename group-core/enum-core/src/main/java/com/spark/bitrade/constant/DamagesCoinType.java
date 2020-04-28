package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 违约金类型
 * @author tansitao
 * @time 2018/7/3 12:01 
 */
@AllArgsConstructor
@Getter
public enum DamagesCoinType implements BaseEnum {

    COIN("币"),//0

    CNY("人民币")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
