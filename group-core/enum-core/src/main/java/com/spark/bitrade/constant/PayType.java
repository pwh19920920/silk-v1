package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 收支类型
 * @author Zhang Yanjun
 * @time 2019.01.09 16:10
 */
@AllArgsConstructor
@Getter
public enum PayType implements BaseEnum {

    PAY("支付"),INCOME("收款");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
