package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 三方支付退款明细退款状态
 * @author shenzucai
 * @time 2018.07.01 17:33
 */
@AllArgsConstructor
@Getter
public enum SilkPayBackStatus implements BaseEnum {

    /**
     * 0 退款中
     */
    BACKING("退款中"),
    /**
     * 1 退款成功
     */
    BACKED("退款成功"),
    /**
     * 2 退款失败
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
