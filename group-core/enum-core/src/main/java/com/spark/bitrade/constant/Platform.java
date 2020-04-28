package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年04月24日
 */
@AllArgsConstructor
@Getter
public enum Platform implements BaseEnum {
    //add|edit|del by  shenzucai 时间： 2018.08.20  原因：添加钱包app版本控制
    ANDROID("安卓"), IOS("苹果"),SILUBIUM_ANDROID("安卓钱包"),SILUBIUM_IOS("苹果钱包"),
    //add by zyj 2019.01.07 : 安卓理财APP 苹果理财APP
    ANDROID_MANAGEMENT("安卓理财APP"),IOS_MANAGEMENT("苹果理财APP"),YITUN_ANDROID("亿豚安卓钱包"),YITUN_IOS("亿豚苹果钱包"),
    DCCPAY_ANDROID("DCCPay安卓钱包"),DCCPAY_IOS("DCCPay苹果钱包");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
