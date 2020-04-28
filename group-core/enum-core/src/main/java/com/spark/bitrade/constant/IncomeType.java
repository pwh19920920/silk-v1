package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 收益类型
 * @author tansitao
 * @time 2018/7/3 12:01 
 */
@AllArgsConstructor
@Getter
public enum IncomeType implements BaseEnum {

    FINANCIAL_A1("锁仓的SLB涨幅超过对应周期的年收益率"),//0
    FINANCIAL_B1("锁仓的SLB涨幅未超过对应周期的年收益率"),
    FINANCIAL_C1("用户强制申请提前解锁")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
