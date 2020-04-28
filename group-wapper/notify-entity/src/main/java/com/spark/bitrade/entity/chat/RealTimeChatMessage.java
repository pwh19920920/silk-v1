package com.spark.bitrade.entity.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.entity.ChatType;
import lombok.Data;
import lombok.ToString;

/**
 * 客户端发送实时消息时的传参规范
 */
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimeChatMessage extends BaseMessage {
    /**
     * 消息内容（默认：中文）
     */
    private String content;
    /**
     * 消息内容（英文）
     */
    private String enContent;
    /**
     * 消息类型拓展
     */
    private ChatType chatType = ChatType.TEXT;
    /**
     * 消息发送方头像
     */
    private String avatar;
    /**
     * 是否推送给发起方，默认true，false表示不推送,是否保存到redis吧/?
     */
    private Boolean sendFromMember = true;
}
