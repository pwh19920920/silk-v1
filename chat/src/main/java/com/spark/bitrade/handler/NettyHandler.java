package com.spark.bitrade.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aqmd.netty.annotation.HawkBean;
import com.aqmd.netty.annotation.HawkMethod;
import com.aqmd.netty.common.NettyCacheUtils;
import com.aqmd.netty.push.HawkPushServiceApi;
import com.spark.bitrade.constant.NettyCommand;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import com.spark.bitrade.netty.QuoteMessage;
//import com.spark.bitrade.service.OrderService;
import com.spark.bitrade.service.OtcOrderService;
import com.spark.bitrade.utils.DateUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashSet;
import java.util.Set;

/**
 * 处理Netty订阅与取消订阅
 */
@Slf4j
@HawkBean
public class NettyHandler {
    @Autowired
    private HawkPushServiceApi hawkPushService;
    @Autowired
    //private OrderService orderService ;
    private OtcOrderService orderService ;

    @Autowired
    private MessageHandler chatMessageHandler ;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ApnsHandler apnsHandler;

    //订阅
    public void subscribeTopic(Channel channel,String topic){
        String userKey = channel.id().asLongText();
        NettyCacheUtils.keyChannelCache.put(channel,userKey);
        NettyCacheUtils.storeChannel(topic,channel);
        if(NettyCacheUtils.userKey.containsKey(userKey)){
            NettyCacheUtils.userKey.get(userKey).add(topic);
        }
        else{
            Set<String> userkeys=new HashSet<>();
            userkeys.add(topic);
            NettyCacheUtils.userKey.put(userKey,userkeys);
        }
    }

    //取消订阅
    public void unsubscribeTopic(Channel channel,String topic){
        String userKey = channel.id().asLongText();
        if(NettyCacheUtils.userKey.containsKey(userKey)) {
            NettyCacheUtils.userKey.get(userKey).remove(topic);
        }
        NettyCacheUtils.keyChannelCache.remove(channel);
    }

    //del by yangch 时间： 2018.04.29 原因：代码合并
    /*@HawkMethod(cmd = NettyCommand.SUBSCRIBE_CHAT,version = NettyCommand.COMMANDS_VERSION)
    public QuoteMessage.SimpleResponse subscribeChat(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        System.out.println("订阅："+json.toJSONString());
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        String orderId = json.getString("orderId");
        String uid = json.getString("uid");
        if(StringUtils.isEmpty(uid) || StringUtils.isEmpty(orderId)){
            response.setCode(500).setMessage("订阅失败，参数错误");
        }
        else {
            String key = orderId + "-" + uid;
            subscribeTopic(ctx.channel(),key);
            response.setCode(0).setMessage("订阅成功");
        }
        return response.build();
    }*/

    //订阅聊天
    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_GROUP_CHAT)
    public QuoteMessage.SimpleResponse subscribeGroupChat(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        log.info("订阅GroupChat：{}", json.toJSONString());
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        String uid = json.getString("uid");
        if(StringUtils.isEmpty(uid)){
            response.setCode(500).setMessage("订阅失败，参数错误：用户ID为空");
        } else {
            String key = uid;
            subscribeTopic(ctx.channel(),key);
            response.setCode(0).setMessage("订阅成功");
        }
        return response.build();
    }

    //del by yangch 时间： 2018.04.29 原因：代码合并
    /*@HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_CHAT)
    public QuoteMessage.SimpleResponse unsubscribeChat(byte[] body, ChannelHandlerContext ctx){
        System.out.println(ctx.channel().id());
        JSONObject json = JSON.parseObject(new String(body));
        String orderId = json.getString("orderId");
        String uid = json.getString("uid");
        String key = orderId+"-"+uid;
        unsubscribeTopic(ctx.channel(),key);
        apnsHandler.removeToken(uid);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("取消订阅成功");
        return response.build();
    }*/

    //取消聊天订阅
    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_GROUP_CHAT)
    public QuoteMessage.SimpleResponse unsubscribeGroupChat(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        log.info("取消订阅GroupChat：{}", json.toJSONString());
        String uid = json.getString("uid");
        String key = uid;
        unsubscribeTopic(ctx.channel(),key);
        apnsHandler.removeToken(uid);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("取消订阅成功");
        return response.build();
    }

    //订阅apns
    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_APNS)
    public QuoteMessage.SimpleResponse subscribeApns(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        log.info("订阅APNS推送：{}", json.toJSONString());
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        String token = json.getString("token");
        String uid = json.getString("uid");
        if(StringUtils.isEmpty(uid) || StringUtils.isEmpty(token)){
            response.setCode(500).setMessage("订阅失败，参数错误");
        }
        else {
            apnsHandler.setToken(uid,token);
            response.setCode(0).setMessage("订阅成功");
        }
        return response.build();
    }

    //取消apns订阅
    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_APNS)
    public QuoteMessage.SimpleResponse unsubscribeApns(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        log.info("取消订阅APNS推送：{}", json.toJSONString());
        String uid = json.getString("uid");
        apnsHandler.removeToken(uid);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("取消订阅成功");
        return response.build();
    }


    //发送聊天消息
    @HawkMethod(cmd = NettyCommand.SEND_CHAT)
    public QuoteMessage.SimpleResponse sendMessage(byte[] body, ChannelHandlerContext ctx){
        log.info("发送消息1：{}", new String(body));
        RealTimeChatMessage message = JSON.parseObject(new String(body), RealTimeChatMessage.class);
        log.info("发送消息2：{}", message);
        handleMessage(message);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("发送成功");
        return response.build();
    }

    //发送聊天消息已读反馈
    @HawkMethod(cmd = NettyCommand.SEND_CHAT_READ_ACK)
    public QuoteMessage.SimpleResponse sendMessageReadAck(byte[] body, ChannelHandlerContext ctx){
        log.info("消息已读反馈1：{}", new String(body));
        RealTimeChatMessage message = JSON.parseObject(new String(body), RealTimeChatMessage.class);
        log.info("消息已读反馈2：{}", message);
        chatMessageHandler.handleReadAckMessage(message);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("发送成功");
        return response.build();
    }


    //生成系统通知订阅key
    private String genNoticeKey(String uid){
        return "n"+uid;
    }
    //订阅系统通知
    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_SYS_NOTICE)
    public QuoteMessage.SimpleResponse subscribeSysNotice(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        System.out.println("订阅Notice："+json.toJSONString());
        log.info("订阅Notice：{}", json.toJSONString());
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        String uid = json.getString("uid");
        if(StringUtils.isEmpty(uid)){
            response.setCode(500).setMessage("订阅失败，参数错误：用户ID为空");
        } else {
            String key = genNoticeKey(uid);
            subscribeTopic(ctx.channel(),key);
            response.setCode(0).setMessage("订阅成功");
        }
        return response.build();
    }
    //取消系统通知
    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_SYS_NOTICE)
    public QuoteMessage.SimpleResponse unsubscribeSysNotice(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        log.info("取消订阅Notice：{}", json.toJSONString());
        String uid = json.getString("uid");
        String key = genNoticeKey(uid);
        unsubscribeTopic(ctx.channel(), key);

        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("取消订阅成功");
        return response.build();
    }

    //发送系统通知
    public void pushSysNotice(RealTimeChatMessage message, boolean handleFlag){
        if(null != message) {
            log.info("发送系统通知：{}", message);
            if(StringUtils.isNotEmpty(message.getUidTo())) {
                if(handleFlag) {
                    chatMessageHandler.handleSysNoticeMessage(message);
                }

                push(genNoticeKey(message.getUidTo()), message, NettyCommand.PUSH_SYS_NOTICE);
                messagingTemplate.convertAndSendToUser(message.getUidTo(), "/sys-notice", message);
            } else {
                log.warn("接收者为空：{}", message);
            }
        }
    }
    public void pushSysNotice(RealTimeChatMessage message){
        pushSysNotice (message, true);
    }


    /**
     * 推送消息
     * @param key
     * @param result
     */
    public void push(String key, Object result,short command) {
        byte[] body = JSON.toJSONString(result, SerializerFeature.WriteEnumUsingToString).getBytes();
        Set<Channel> channels = NettyCacheUtils.getChannel(key);
        log.info("推送消息：key={},result={}", key, new String(body));
        if(channels!=null && channels.size() > 0) {
            log.info("下发消息：key={},channel size={}", key, channels.size());
            hawkPushService.pushMsg(channels, command, body);
        }
    }

    //处理 c2c支付通知、聊天通知和C2C事件通知
    public void handleMessage(RealTimeChatMessage message){
        if(message.getMessageType()==MessageTypeEnum.NOTICE){
            //查询订单
            Order order =  orderService.findOneByOrderId(message.getOrderId());
            ConfirmResult result = new ConfirmResult(message.getContent(),order.getStatus().getOrdinal());
            result.setUidFrom(message.getUidFrom());
            result.setOrderId(message.getOrderId());
            result.setNameFrom(message.getNameFrom());

            //del by yangch 时间： 2018.04.29 原因：代码合并
            //push(message.getOrderId() + "-" + message.getUidTo(),result,NettyCommand.PUSH_CHAT);
            push(message.getUidTo(), result, NettyCommand.PUSH_GROUP_CHAT);
            messagingTemplate.convertAndSendToUser(message.getUidTo(),"/order-notice/"+message.getOrderId(),result);
        } else if(message.getMessageType() == MessageTypeEnum.NORMAL_CHAT ||
                message.getMessageType() == MessageTypeEnum.OTC_EVENT) {
            //edit by yangch 时间： 2018.12.19 原因：c2c聊天信息中包含 聊天消息 和 c2c事件流转消息
            ChatMessageRecord chatMessageRecord = new ChatMessageRecord();
            BeanUtils.copyProperties(message, chatMessageRecord);
            chatMessageRecord.setSendTime(DateUtils.getCurrentDate().getTime());
            chatMessageRecord.setFromAvatar(message.getAvatar());
            chatMessageRecord.setSendFromMember(message.getSendFromMember());
            //保存聊天消息（记录保存到mogondb库）
            chatMessageHandler.handleMessage(chatMessageRecord);
            chatMessageRecord.setSendTimeStr(DateUtils.getDateStr(chatMessageRecord.getSendTime()));

            //发送给指定用户（客户端订阅路径：/user/+uid+/+key）
            push(message.getUidTo(), chatMessageRecord, NettyCommand.PUSH_GROUP_CHAT);
            //del by yangch 时间： 2018.04.29 原因：代码合并
            //push(message.getOrderId() + "-" + message.getUidTo(),chatMessageRecord,NettyCommand.PUSH_CHAT);

            //del by yangch 时间： 2018.12.19 原因：无apns消息推送
            ///apnsHandler.handleMessage(message.getUidTo(),chatMessageRecord);
            messagingTemplate.convertAndSendToUser(message.getUidTo(), "/" + message.getOrderId(), chatMessageRecord);
        }
    }


    /**
     * 处理发送红点消息
     * @param
     */
    public void handleRedMessage( String uid, String content){
        log.info("==================开始发送===================");
        messagingTemplate.convertAndSendToUser(uid,"/redPoint",content);
        log.info("==================发送成功==================");
    }
}
