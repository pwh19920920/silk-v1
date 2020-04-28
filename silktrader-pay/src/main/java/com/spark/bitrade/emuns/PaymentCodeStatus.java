package com.spark.bitrade.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>付款码状态</p>
 * @author tian.bo
 * @since 2019/3/11
 */
@AllArgsConstructor
@Getter
public enum PaymentCodeStatus {

    WAIT_BUYER_PAY(1,"待支付"),PAY_FINISHED(2,"已支付"),PAY_INVALID(3,"已失效");

    @Setter
    private int code;

    @Setter
    private String name;

}
