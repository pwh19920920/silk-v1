package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 交易类型
 * @author Zhang Yanjun
 * @time 2019.01.09 16:10
 */
@AllArgsConstructor
@Getter
public enum PayTransferType implements  BaseEnum ,IEnum {
    UNKNOWN(0,"未知"),

    //pay_fast_record表会使用以下3个状态
    PAY_FAST(1,"云端支付--（云端钱包<->云端钱包）"),
    EXCHANGE(2,"兑换"),
    PAYMENT_CODE(3,"支付码支付"),

    //以下状态在记录聚合时会使用到
    C2C(4,"法币交易"),
    ASSET_TRANSFER(5,"资产划转"),
    PAY_SLOW(6,"云端支付--（区块链钱包<->云端钱包）");

// 1、云端支付--（云端钱包->云端钱包）
//    1.1、云端收款（云端钱包->云端钱包）  ----当前用户ID=收款用户ID（receiptId）
//    1.2、云端转账（云端钱包->云端钱包）  ----当前用户ID=支付用户ID（payId）
// 2、兑换交易
//   2.1、兑换token   ----当前用户ID=收款用户ID（receiptId）
//   2.2、token兑换   ----当前用户ID=支付用户ID（payId）
// 3、支付码支付
//   3.1、扫支付码收款   ----当前用户ID=收款用户ID（receiptId）
//   3.2、被扫支付码付款   ----当前用户ID=支付用户ID（payId）
//
//
// 4、法币交易
//   4.1、法币交易（出售）   ----当前用户ID=收款用户ID（receiptId）
//   4.2、法币交易（购买）   ----当前用户ID=支付用户ID（payId）
// 5、资产划转
//   5.1、资产划转（云端钱包->区块链钱包）   ----当前用户ID=收款用户ID（receiptId）
//   5.2、资产划转（区块链钱包->云端钱包）   ----当前用户ID=支付用户ID（payId）
//
// 6、云端支付--（区块链钱包->云端钱包）
//  6.1、云端收款（区块链钱包->云端钱包）   ----当前用户ID=收款用户ID（receiptId）
//  6.2、云端转账（区块链钱包->云端钱包）   ----当前用户ID=支付用户ID（payId）


    @Setter
    private int id;
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.id;
    }

    @Override
    public Serializable getValue() {
        return this.id;
    }
}
