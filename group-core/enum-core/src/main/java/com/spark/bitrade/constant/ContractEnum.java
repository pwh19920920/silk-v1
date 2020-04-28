package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 三方支付签约状态
 * @author shenzucai
 * @time 2018.07.02 7:59
 */
@AllArgsConstructor
@Getter
public enum ContractEnum implements BaseEnum {
    IS_FALSE(false, "禁用"),
    IS_TRUE(true, "启用");

    @Setter
    private boolean is;

    @Setter
    private String nameCn;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
