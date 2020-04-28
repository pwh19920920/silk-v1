package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 三方支付订单状态
 * @author shenzucai
 * @time 2018.07.01 17:33
 */
@AllArgsConstructor
@Getter
public enum SilkPayOrderStatus implements BaseEnum {

    /**
     * 0 未支付
     */
    NONPAYMENT("未支付"),
    /**
     * 1 已支付
     */
    PAID("已支付"),
    /**
     * 2 已取消
     */
    CANCELLED("已取消"),
    /**
     * 3 支付中
     */
    PAYING("支付中")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
}
