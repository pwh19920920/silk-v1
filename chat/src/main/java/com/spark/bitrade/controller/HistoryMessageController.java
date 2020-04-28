package com.spark.bitrade.controller;

import com.spark.bitrade.entity.HistoryChatMessage;
import com.spark.bitrade.entity.HistoryMessagePage;
import com.spark.bitrade.handler.MessageHandler;
import com.spark.bitrade.notice.INoticeService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryMessageController {

    @Autowired
    private MessageHandler chatMessageHandler;
    @Autowired
    @Qualifier("chatNoticeServiceImpl")
    private INoticeService chatNoticeServiceImpl;
    @Autowired
    @Qualifier("otcEventNoticeServiceImpl")
    private INoticeService otcEventNoticeServiceImpl;
    @Autowired
    @Qualifier("sysNoticeServiceImpl")
    private INoticeService sysNoticeServiceImpl;

    /**
     * 获取历史聊天记录
     *
     * @param message
     * @param uidTo 会员ID
     * @return
     */
    @RequestMapping("/getHistoryMessage/news")
    public HistoryMessagePage getHistoryMessageNews(HistoryChatMessage message, String uidTo) {
        return chatMessageHandler.getHistoryMessageNews(message, uidTo);
    }

    /**
     * 获取历史聊天记录(已废弃)
     *
     * @param message
     * @return
     */
    @RequestMapping("/getHistoryMessage")
    public HistoryMessagePage getHistoryMessage(HistoryChatMessage message) {
        return chatMessageHandler.getHistoryMessage(message);
    }

    /**
     * 获取聊天通知
     *
     * @param uid 用户ID
     * @return
     */
    @RequestMapping("/getNotice4Chat")
    public MessageResult getNotice4Chat(String uid) {
        return MessageResult.success("success", chatNoticeServiceImpl.list(uid));
    }

    /**
     * 获取聊天通知数量
     *
     * @param uid 用户ID
     * @return
     */
    @RequestMapping("/getNoticeCnt4Chat")
    public MessageResult getNoticeCnt4Chat(String uid) {
        return MessageResult.success("success", chatNoticeServiceImpl.count(uid));
    }

    /**
     * 获取Otc事件流转通知
     *
     * @param uid 用户ID
     * @return
     */
    @RequestMapping("/getNotice4OtcEvent")
    public MessageResult getNotice4OtcEvent(String uid) {
        return MessageResult.success("success", otcEventNoticeServiceImpl.list(uid));
    }

    /**
     * 获取Otc事件流转通知数量
     *
     * @param uid 用户ID
     * @return
     */
    @RequestMapping("/getNoticeCnt4OtcEvent")
    public MessageResult getNoticeCnt4OtcEvent(String uid) {
        return MessageResult.success("success", otcEventNoticeServiceImpl.count(uid));
    }

    /**
     * 获取系统通知
     *
     * @param uid 用户ID
     * @return
     */
    @RequestMapping("/getNotice4Sys")
    public MessageResult getNotice4sys(String uid) {
        return MessageResult.success("success", sysNoticeServiceImpl.list(uid));
    }

    /**
     * 获取系统通知数量
     *
     * @param uid 用户ID
     * @return
     */
    @RequestMapping("/getNoticeCnt4Sys")
    public MessageResult getNoticeCnt4Sys(String uid) {
        return MessageResult.success("success", sysNoticeServiceImpl.count(uid));
    }
}
