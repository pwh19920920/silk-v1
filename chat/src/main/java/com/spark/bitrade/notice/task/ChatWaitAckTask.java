package com.spark.bitrade.notice.task;

import com.spark.bitrade.entity.ChatMessageRecord;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/***
 * 聊天信息已读取确认任务
 * @author yangch
 * @time 2018.12.21 14:40
 */

public class ChatWaitAckTask extends WaitAckTask {
    private static Long delayTime = 10L;          //延迟时间，默认为10秒
    private static TimeUnit delayTimeUnit = TimeUnit.SECONDS;  //延迟时间单位

    public ChatWaitAckTask(ChatMessageRecord message) {
        super(delayTime, delayTimeUnit,message.getOrderId() + "-"+message.getUidTo(), message);
    }

    public ChatWaitAckTask(String taskId, ChatMessageRecord message) {
        super(delayTime, delayTimeUnit, taskId, message);
    }

    public ChatWaitAckTask(long delayTime, TimeUnit delayTimeUnit, ChatMessageRecord message) {
        super(delayTime, delayTimeUnit, message);
    }

    public ChatWaitAckTask(long delayTime, TimeUnit delayTimeUnit, String taskId, ChatMessageRecord message) {
        super(delayTime, delayTimeUnit, taskId, message);
    }
}
