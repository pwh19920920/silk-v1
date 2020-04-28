package com.spark.bitrade.service;

import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/***
 * 提供推送系统通知消息服务
 * @author yangch
 * @time 2018.11.30 11:10
 */
@FeignClient("service-chat")
public interface IPushSysNoticeService {

    /**
     * 推送系统通知
     * @param message
     */
    @RequestMapping("/chat/message/pushNotice/entity")
    void pushNotice(@RequestParam(value = "message") RealTimeChatMessage message);

    /**
     * 推送系统通知消息
     * @author yangch
     * @time 2018.11.30 11:26 
     * @param jsonMessage RealTimeChatMessage对象的json字符串
     */
    @RequestMapping("/chat/message/pushNotice/json")
    void pushNotice(@RequestParam(value = "jsonMessage") String jsonMessage);
}
