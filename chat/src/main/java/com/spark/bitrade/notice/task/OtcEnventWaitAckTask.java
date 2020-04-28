package com.spark.bitrade.notice.task;

import com.spark.bitrade.entity.ChatMessageRecord;

import java.util.concurrent.TimeUnit;

/***
  * Otc订单流转事件信息已读取确认任务
  * @author yangch
  * @time 2018.12.21 14:40
  */
public class OtcEnventWaitAckTask extends WaitAckTask {
    private static Long delayTime = 3L;          //延迟时间，默认为3秒
    private static TimeUnit delayTimeUnit = TimeUnit.SECONDS;  //延迟时间单位

    public OtcEnventWaitAckTask(ChatMessageRecord message) {
        super(delayTime, delayTimeUnit, message.getOrderId() + "-" + message.getUidTo(), message);
    }

    public OtcEnventWaitAckTask(String taskId, ChatMessageRecord message) {
        super(delayTime, delayTimeUnit, taskId, message);
    }

    public OtcEnventWaitAckTask(long delayTime, TimeUnit delayTimeUnit, ChatMessageRecord message) {
        super(delayTime, delayTimeUnit, message);
    }

    public OtcEnventWaitAckTask(long delayTime, TimeUnit delayTimeUnit, String taskId, ChatMessageRecord message) {
        super(delayTime, delayTimeUnit, taskId, message);
    }
}
