package com.spark.bitrade.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Zhang Jinwei
 * @date 2018年01月20日
 */
@Builder
@Data
public class PayInfo {
    private String realName;
    private Alipay alipay;
    private WechatPay wechatPay;
    private BankInfo bankInfo;
    private Epay epay;
}
