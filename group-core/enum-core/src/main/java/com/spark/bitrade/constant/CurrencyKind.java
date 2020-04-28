package com.spark.bitrade.constant;

/**
 * 充值货币种类
 */
//del by yangch 时间： 2018.04.21 原因：同步代码时发现该类已删除

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CurrencyKind {
    RMB("人民币"), SGD("新加坡币");
    String cnName;
}
