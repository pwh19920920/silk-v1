package com.spark.bitrade.notice.impl;
import com.spark.bitrade.entity.ChatMessageRecord;
import com.spark.bitrade.entity.MessageTypeEnum;

import com.spark.bitrade.entity.chat.BaseMessage;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import com.spark.bitrade.handler.NettyHandler;
import com.spark.bitrade.notice.INoticeService;
import com.spark.bitrade.notice.IWaitAckTaskService;
import com.spark.bitrade.notice.task.ChatWaitAckTask;
import com.spark.bitrade.notice.task.OtcEnventWaitAckTask;
import com.spark.bitrade.notice.task.WaitAckTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;

/***
 * 等待确认的任务服务
 * @author yangch
 * @time 2018.12.21 17:08
 */
@Service
@Slf4j
public class WaitAckTaskService implements IWaitAckTaskService {
    @Autowired
    private NettyHandler nettyHandler;
    @Autowired
    @Qualifier("chatNoticeServiceImpl")
    private INoticeService chatNoticeServiceImpl;
    @Autowired
    @Qualifier("otcEventNoticeServiceImpl")
    private INoticeService otcEventNoticeServiceImpl;
    @Autowired
    @Qualifier("sysNoticeServiceImpl")
    private INoticeService sysNoticeServiceImpl;

    //默认的延迟队列
    protected DelayQueue<WaitAckTask> queue = new DelayQueue<>();

    @Override
    public DelayQueue<WaitAckTask> getQueue() {
        return queue;
    }

    /**
     * 添加任务
     * @param task 任务
     */
    @Override
    public void addTask(WaitAckTask task){
        queue.remove(task);
        queue.add(task);
    }

    /**
     * 移除任务
     * @param task 任务
     * @return
     */
    @Override
    public boolean removeTask(WaitAckTask task){
        return this.queue.remove(task);
    }

    /**
     * 处理收到的已读回复
     * @param message
     */
    @Override
    public void executeReceiveAck(BaseMessage message) {
        log.info("已读反馈：{}", message);
        //收到消息后移除延迟队列中的待处理任务，三种消息类型：系统消息、聊天和OTC事件消息
        ChatMessageRecord chatMessageRecord = new ChatMessageRecord();
        BeanUtils.copyProperties(message, chatMessageRecord);

        if(message.getMessageType() == MessageTypeEnum.NORMAL_CHAT) {
            //聊天消息
            removeTask(new ChatWaitAckTask(chatMessageRecord));
            chatNoticeServiceImpl.ack(message.getUidFrom(), message.getOrderId());
        } else if(message.getMessageType() == MessageTypeEnum.OTC_EVENT) {
            //OTC事件消息
            removeTask(new OtcEnventWaitAckTask(chatMessageRecord));
            otcEventNoticeServiceImpl.ack(message.getUidFrom(), message.getOrderId());
        } else if(message.getMessageType() == MessageTypeEnum.SYSTEM_MES) {
            //系统消息
            sysNoticeServiceImpl.ack(message.getUidFrom(), message.getOrderId());
        } else if(message.getMessageType() == MessageTypeEnum.UNKNOWN){
            //兼容pc的已读反馈

            //聊天消息
            removeTask(new ChatWaitAckTask(chatMessageRecord));
            chatNoticeServiceImpl.ack(message.getUidFrom(), message.getOrderId());

            //OTC事件消息
            removeTask(new OtcEnventWaitAckTask(chatMessageRecord));
            otcEventNoticeServiceImpl.ack(message.getUidFrom(), message.getOrderId());
        } else {
            log.warn("未知的消息类型，消息：{}", message);
        }
    }

    /**
     * 执行延迟任务
     * @param task
     */
    @Override
    //@Async
    public void executeWaitAckTask(WaitAckTask task) {
        //限制的时间内未收到已读消息的确认（聊天消息和事件消息采用系统推送渠道再次推送消息），可以按历史未读消息处理（入库保存 或者 保存到Redis缓存中）
        //System.out.println(task.getTaskId() + ":--333333--------:" + System.currentTimeMillis());
        log.info("执行延迟任务:"+ task);
        if(task.getMessage() != null) {
            //限制的时间内未收到已读消息确认，升级推送渠道（使用系统通知渠道推送通知）
            RealTimeChatMessage message = new RealTimeChatMessage();
            BeanUtils.copyProperties(task.getMessage(), message);

            //聊天和OTC事件消息的消息需要先保存（需要按订单号去重），再获取通知数量
            if(message.getMessageType() == MessageTypeEnum.NORMAL_CHAT) {
                //聊天消息，保存到缓存
                chatNoticeServiceImpl.save(message);
            } else if(message.getMessageType() == MessageTypeEnum.OTC_EVENT) {
                //OTC事件消息，保存到缓存
                otcEventNoticeServiceImpl.save(message);
            }
            message.setContent(String.valueOf(getNotice4OtcCount(message.getUidTo())));   //未读数量（事件消息+聊天消息）并按订单号去重

            nettyHandler.pushSysNotice(message);
        } else {
            log.warn("执行延迟任务的Message为null");
        }
    }

    //合并聊天和otc事件流转通知
    @Override
    public int getNotice4OtcCount(String uid){
        List<String> lst = new ArrayList<>();

        //聊天通知
        List<String> lstChat= chatNoticeServiceImpl.list(uid);
        lst.addAll(lstChat);

        //otc事件流转通知
        List<String> setOtc = otcEventNoticeServiceImpl.list(uid);
        if(setOtc != null) {
            setOtc.forEach(s->{
                if(!lst.contains(s)) {
                    lst.add(String.valueOf(s));
                }
            });
        }

        return lst.size();
    }
}
