package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年02月26日
 */
@AllArgsConstructor
@Getter
public enum LoginType implements BaseEnum {

    WEB("WEB登录"),//0

    ANDROID("Android登录"), //1

    IOS("IOS登录"),//2
    API("API接入")    //3
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }

}
