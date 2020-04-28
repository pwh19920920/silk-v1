package com.spark.bitrade.controller.vo;

import lombok.Data;

/**
 * VirtualTraderVo
 *
 * @author Archx[archx@foxmail.com]
 * @since 2019/6/28 15:46
 */
@Data
public class VirtualTraderVo {

    private String symbol;
    private String amount;
    private String price;
}
