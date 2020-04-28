package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 短信发送状态
 * @author tansitao
 * @time 2018/9/19 19:19 
 */
@AllArgsConstructor
@Getter
public enum SmsStatus implements BaseEnum {
    WAITING(0, "待发送"),
    success(1, "成功"),
    FAIL(2,"失败");

    //枚举，发送状态：待发送、成功、失败

    @Setter
    private int code;

    @Setter
    private String nameCn;

    @Override
    @JsonValue
    public int getOrdinal() {
        return code;
    }
}
