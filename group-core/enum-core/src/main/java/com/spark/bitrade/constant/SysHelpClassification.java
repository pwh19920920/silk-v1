package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author rongyu
 * @description
 * @date 2018/1/9 9:42
 */
@AllArgsConstructor
@Getter
public enum SysHelpClassification implements BaseEnum {

    HELP("新手入门"), //0

    FAQ("常见问题"), //1

    RECHARGE("充值指南"), //2

    TRANSACTION("交易指南"), //3

    QR_CODE("APP二维码"), //4

    COIN_INFO("币种资料") //5
    ;

    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
