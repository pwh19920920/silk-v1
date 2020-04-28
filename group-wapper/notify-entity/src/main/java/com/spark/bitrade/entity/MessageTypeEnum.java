package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum MessageTypeEnum implements BaseEnum{

    /**
     * 提醒对方刷新订单页面
     */
    NOTICE("确认付款"), //0
    /**
     * 聊天
     */
    NORMAL_CHAT("正常聊天"),    //1
    SYSTEM_MES("系统消息"),     //2
    OTC_EVENT("OTC事件消息"),   //3
    UNKNOWN("未知")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }

    @Override
    public String toString() {
        return String.valueOf(this.getOrdinal());
    }
}
