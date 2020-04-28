package com.spark.bitrade.constant;

import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lingxing
 * @time 2018.08.01 16:10
 */
@AllArgsConstructor
@Getter
public enum  SmsSendStatus implements BaseEnum {


    /**
     * 未发送短信
     */
    NO_SMS_SEND("未发送短信"),
    /**
     * 已发送短信
     */
    ALREADY_SMS_SEND("已发送短信"),
    /**
     *发送短信失败
     */
    FAIL_SEND_SMS("发送短信失败"),
    ;


    @Setter
    private String cnName;

    @Override
    public int getOrdinal() {
        return ordinal();
    }
}
