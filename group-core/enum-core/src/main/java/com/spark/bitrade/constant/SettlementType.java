package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 理财结算类型
 * @author tansitao
 * @time 2018/7/3 12:01 
 */
@AllArgsConstructor
@Getter
public enum SettlementType implements BaseEnum {

    ACTIVITY_COIN("活动币种结算"),//0

    USDT_SETTL("USDT结算")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
