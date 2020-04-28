package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import com.spark.bitrade.handler.MessageHandler;
import com.spark.bitrade.handler.NettyHandler;
import com.spark.bitrade.notice.IWaitAckTaskService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WebSocketController {
    //private  final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    @Autowired
    private NettyHandler nettyHandler;

    @Autowired
    private MessageHandler chatMessageHandler ;
    @Autowired
    private IWaitAckTaskService iWaitAckTaskService;

    /**
     * ORDER-GENERATED:若接收到该内容，发送订单广告的自动回复（如果广告设置了自动回复的话）
     * @param message
     */
    @MessageMapping("/message/chat")
    public void chat(RealTimeChatMessage message){
        log.info("message={}", message);
        nettyHandler.handleMessage(message);
    }

    /**
     * 提供发送消息的接口
     * @param message RealTimeChatMessage类
     */
    @RequestMapping("/message/pushChat/entity")
    public void chatPush(RealTimeChatMessage message){
        chat(message);
    }
    /**
     * 提供发送消息的接口
     * @param jsonMessage RealTimeChatMessage对象的json格式
     */
    @RequestMapping("/message/pushChat/json")
    public void chatPush(String jsonMessage){
        if(StringUtils.isEmpty(jsonMessage)){
            log.info("message is null");
            return;
        }

        RealTimeChatMessage chatMessage =JSON.parseObject(jsonMessage, RealTimeChatMessage.class);
        chat(chatMessage);
    }



    /**
     * 推送系统消息
     * @param message
     */
    @MessageMapping("/message/notice")
    public void notice(RealTimeChatMessage message){
        log.info("message={}",message);
        if(message != null
                || message.getMessageType() == MessageTypeEnum.SYSTEM_MES) {
            nettyHandler.pushSysNotice(message);
        } else {
            log.info("推送的消息为null或消息类型不为SYSTEM_MES");
        }
    }

    /**
     * 提供推送系统通知消息的接口
     * @param message RealTimeChatMessage类
     */
    @RequestMapping("/message/pushNotice/entity")
    public void pushNotice(RealTimeChatMessage message){
        notice(message);
    }

    /**
     * 提供推送系统通知消息的接口
     * @param jsonMessage RealTimeChatMessage对象的json格式
     */
    @RequestMapping("/message/pushNotice/json")
    public void pushNotice(String jsonMessage){
        if(StringUtils.isEmpty(jsonMessage)){
            log.info("message is null");
            return;
        }

        RealTimeChatMessage message =JSON.parseObject(jsonMessage, RealTimeChatMessage.class);
        notice(message);
    }



    /**
     * 读取消息的websocket反馈接口(包含聊天、otc事件、系统通知)
     * 接口：/app/message/readAck
     * @param message
     */
    @MessageMapping("/message/readAck")
    public void readAck4ws(RealTimeChatMessage message){
        chatMessageHandler.handleReadAckMessage(message);
        //iWaitAckTaskService.executeReceiveAck(message);
    }

    /**
     * 读取消息的反馈(包含聊天、otc事件、系统通知)
     * @param message
     */
    @RequestMapping("/message/readAck")
    public MessageResult readAck(RealTimeChatMessage message){
        readAck4ws(message);

        //未读数量（事件消息+聊天消息）并按订单号去重
        message.setContent(String.valueOf(iWaitAckTaskService.getNotice4OtcCount(message.getUidTo())));
        nettyHandler.pushSysNotice(message, false);

        return MessageResult.success("success");
    }

    /**
     * 发送红点消息
     * @param uid 被通知的用户id
     * @param content 红点数字
     */
    @RequestMapping(value = "/message/sendRedPoint",method = RequestMethod.POST)
    public void sendRedPoint(@RequestParam(value = "uid") String uid, @RequestParam(value = "content") String content){
        nettyHandler.handleRedMessage(uid,content);
    }
}
