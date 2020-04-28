package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author tansitao
 * @time 2018.04.05 17:11
 */
@Builder
@Data
public class BusinessInfo {

    /**
     * 安全信息
     */
    private String username;
    private long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private BooleanEnum realVerified;//是否实名认证
    private BooleanEnum emailVerified;//是否邮件认证
    private BooleanEnum phoneVerified;//是否手机认证
    private BooleanEnum loginVerified;//是否登录认证
    private BooleanEnum fundsVerified;//是否资金密码设置
    private BooleanEnum realAuditing; //当前商家材料认证过程
    private String mobilePhone;
    private String email;
    private String realName;
    private String realNameRejectReason;
    private String idCard;
    private String avatar;

    /**
     * 账户信息
     */
    private BooleanEnum bankVerified; //是否银行认证
    private BooleanEnum aliVerified; //是否支付宝认证
    private BooleanEnum wechatVerified; //是否微信认证
    private BankInfo bankInfo;
    private Alipay alipay;
    private WechatPay wechatPay;

    private BooleanEnum marginVerified; //是否缴纳保证金
}
