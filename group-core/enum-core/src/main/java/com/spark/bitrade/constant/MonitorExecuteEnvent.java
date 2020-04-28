package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 交易平台用户权限枚举
 *
 * @author yangch
 * @time 2018.11.01 9:34
 * @since 1.3版本新增
 */
@AllArgsConstructor
@Getter
public enum MonitorExecuteEnvent implements BaseEnum {

    /**
     * 无操作
     */
    NONE("NONE","无操作"), //0
    //权限：登陆、发布广告、C2C交易、币币交易、充值提币
    //登陆权限
    ALLOW_LOGIN("LOGIN","允许登陆"),
    FORBID_LOGIN("LOGIN","禁止登陆"),   //2

    ALLOW_TRADE("TRADE","允许交易"),    //3
    FORBID_TRADE("TRADE","禁止交易"),   //4 禁止交易后，不允许卖出、币币交易和提币操作

    //OTC权限
    ALLOW_OTC("OTC","允许C2C交易"),   //5
    FORBID_OTC("OTC","禁止C2C交易"),  //6
    ALLOW_OTC_BUY("OTC","允许C2C买入交易"), //7 1.3版本需求新增
    FORBID_OTC_BUY("OTC","禁止C2C买入交易"),    //8 1.3版本需求新增
    ALLOW_OTC_SELL("OTC","允许C2C卖出交易"),    //9 1.3版本需求新增
    FORBID_OTC_SELL("OTC","禁止C2C卖出交易"),   //10 1.3版本需求新增

    ALLOW_ADVERTISE("OTC","允许发布广告"),  //11
    FORBID_ADVERTISE("OTC","禁止发布广告"), //12

    //币币交易权限
    ALLOW_EXCHANGE("EXCHANGE","允许币币交易"),   //13
    FORBID_EXCHANGE("EXCHANGE","禁止币币交易"),  //14
    ALLOW_EXCHANGE_BUY("EXCHANGE","允许币币买入交易"),     //15 1.3版本需求新增
    FORBID_EXCHANGE_BUY("EXCHANGE","禁止币币买入交易"),    //16 1.3版本需求新增
    ALLOW_EXCHANGE_SELL("EXCHANGE","允许币币卖出交易"),    //17 1.3版本需求新增
    FORBID_EXCHANGE_SELL("EXCHANGE","禁止币币卖出交易"),   //18 1.3版本需求新增

    //充提币权限
    ALLOW_COIN("COIN","允许充值提币"),   //19
    FORBID_COIN("COIN","禁止充值提币"),  //20
    ALLOW_COIN_IN("COIN","允许充值"),    //21
    FORBID_COIN_IN("COIN","禁止充值"),   //22
    ALLOW_COIN_OUT("COIN","允许提币"),   //23
    FORBID_COIN_OUT("COIN","禁止提币"),   //24

    FORBID_MOBILE_TRANSACTIONS_STATUS("UC","修改手机24小时内禁止交易"),
    ALLOW_MOBILE_TRANSACTIONS_STATUS("UC","修改手机24小时后解冻"),

    FORBID_EMAIL_TRANSACTIONS_STATUS("UC","修改邮箱24小时内禁止交易"),
    ALLOW_EMAIL_TRANSACTIONS_STATUS("UC","修改邮箱24小时后解冻"),

    //告警
    WARN("WARN","告警通知")
    ;

    @Setter
    private String cnType;  //类型
    @Setter
    private String cnName;  //中文描述

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
