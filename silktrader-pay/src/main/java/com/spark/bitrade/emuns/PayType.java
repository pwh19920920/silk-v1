package com.spark.bitrade.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Administrator on 2019/3/11.
 */
@AllArgsConstructor
@Getter
public enum PayType {

    BARCODEPAY(3,"条码支付");


    @Setter
    private int code;

    @Setter
    private String name;


}
