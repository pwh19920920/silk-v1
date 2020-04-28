package com.spark.bitrade.service;

import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/***
 * 提供chat服务
 * @author yangch
 * @time 2018.11.30 11:10
 */
@FeignClient("service-chat")
public interface IChatService {

    /**
     * 发送聊天消息
     * @param message
     */
    @RequestMapping("/chat/message/pushChat/entity")
    void chatPush(@RequestParam(value = "message") RealTimeChatMessage message);

    /**
     * 发送聊天消息
     * @author yangch
     * @time 2018.11.30 11:26 
     * @param jsonMessage RealTimeChatMessage对象的json字符串
     */
    @RequestMapping("/chat/message/pushChat/json")
    void chatPush(@RequestParam(value = "jsonMessage") String jsonMessage);
}
