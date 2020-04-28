package com.spark.bitrade.entity;

import com.spark.bitrade.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author Zhang Jinwei
 * @date 2018年01月16日
 */
@Builder
@Data
public class MemberAccount {
    private String realName;
    private BooleanEnum bankVerified;
    private BooleanEnum aliVerified;
    private BooleanEnum wechatVerified;
    private BooleanEnum epayVerified;
    private BankInfo bankInfo;
    private Alipay alipay;
    private WechatPay wechatPay;
    private Epay epay;//add by tansitao 时间： 2018/8/16 原因：增加epay
    private int transactions;//交易次数
}
