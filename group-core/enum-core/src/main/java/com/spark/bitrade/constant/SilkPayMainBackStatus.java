package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 三方支付交易记录退款状态
 * @author shenzucai
 * @time 2018.07.01 17:33
 */
@AllArgsConstructor
@Getter
public enum SilkPayMainBackStatus implements BaseEnum {


    /**
     * 0 未申请退款
     */
    UNAPPLYBACK("未申请退款"),
    /**
     * 1 申请退款
     */
    APPLYBACK("申请退款"),
    /**
     * 2 退款中
     */
    BACKING("退款中"),
    /**
     * 3 退款成功
     */
    BACKED("退款成功"),
    /**
     * 4 退款失败
     */
    BACKFAILED("退款失败");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
}
