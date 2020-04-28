package com.spark.bitrade.handler;

import com.spark.bitrade.entity.ChatMessageRecord;
import com.spark.bitrade.entity.HistoryChatMessage;
import com.spark.bitrade.entity.HistoryMessagePage;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;


public interface MessageHandler {

    /**
     * 处理聊天消息
     * @param message
     */
    void handleMessage(ChatMessageRecord message);

    /**
     * 获取历史聊天记录
     *
     * @param message
     * @return
     */
    HistoryMessagePage getHistoryMessage(HistoryChatMessage message);

    /**
     * 获取历史聊天记录
     *
     * @param message
     * @param uidTo    会员ID
     * @return
     */
    HistoryMessagePage getHistoryMessageNews(HistoryChatMessage message,String uidTo);

    /**
     * 处理系统通知消息
     * @param message
     */
    void handleSysNoticeMessage(RealTimeChatMessage message);

    //消息已读反馈
    void handleReadAckMessage(RealTimeChatMessage message);
}
