package com.spark.bitrade.model.screen;

import lombok.Data;

/**
 * @author fumy
 * @time 2018.09.06 11:29
 */
@Data
public class ExchangeCoinScreen {

    private String symbol;  //交易对

    private String coinSymbol;//交易币种

    private String baseSymbol;//结算币种
}
