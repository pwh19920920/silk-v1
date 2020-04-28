package com.spark.bitrade.entity;

import com.spark.bitrade.entity.chat.BaseMessage;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 历史聊天记录
 */

@Data
@ApiModel
public class HistoryChatMessage extends BaseMessage{

    private int limit = 20 ;
    private int page = 1;
    //发送时间
    private String sendTime;
    //消息内容
    private String content;
}
