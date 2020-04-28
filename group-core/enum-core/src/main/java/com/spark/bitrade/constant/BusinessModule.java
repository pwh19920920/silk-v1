package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 业务模块
 * @author yangch
 * @time 2018.06.05 13:54
 */
@AllArgsConstructor
@Getter
public enum BusinessModule implements BaseEnum {
    OTC("C2C交易"),
    EXCHANGE("币币交易");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
