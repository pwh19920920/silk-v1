package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.entity.chat.BaseMessage;
import lombok.Data;
import lombok.ToString;

/**
 * mogondb保存聊天消息的格式规范
 */

@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageRecord extends BaseMessage {
    /**
     * 推送消息内容（默认：中文）
     */
    private String content;
    /**
     * 推送消息内容（英文）
     */
    private String enContent;
    /**
     * 消息类型拓展
     */
    private ChatType chatType = ChatType.TEXT;
    /**
     * 发送时间
     */
    private long sendTime;

    private String sendTimeStr;

    private String fromAvatar;
    /**
     * 是否推送给发起方，默认true，false表示不推送
     */
    private Boolean sendFromMember = true;
}
