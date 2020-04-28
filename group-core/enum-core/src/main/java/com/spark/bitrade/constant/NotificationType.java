package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知渠道类型
 * @author tansitao
 * @time 2018/12/18 11:00 
 */
@AllArgsConstructor
@Getter
public enum NotificationType implements BaseEnum {
    SMS("短信通知"),//0
    EMAIL("邮件通知"),//1
    SYSTEM("系统消息"),//2
    APNS("离线消息"),//3
    C2C_CHAT("C2C聊天");//4

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
