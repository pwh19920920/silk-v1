package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *  付款码策略类型
 *
 * @author yangch
 * @time 2019.03.01 13:53
 */
@AllArgsConstructor
@Getter
public enum PaymentCodeStrategyType implements BaseEnum {
    /**
     * 0 未配置
     */
    NONE("未配置"),
    CYCLE_REFRESH("定时自动刷新"),
    ONCE("每次用后失效")
    //1、定时自动刷新 2、每次用后失效 3、交易安全保障
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
}
