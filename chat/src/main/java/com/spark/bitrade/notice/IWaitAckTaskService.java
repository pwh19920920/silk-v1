package com.spark.bitrade.notice;

import com.spark.bitrade.entity.chat.BaseMessage;
import com.spark.bitrade.notice.task.WaitAckTask;

import java.util.concurrent.DelayQueue;

/***
 * 等待确认的延迟任务服务接口定义
 *
 * @author yangch
 * @time 2018.12.21 17:39
 */
public interface IWaitAckTaskService {
    //获取默认的延迟队列
    DelayQueue<WaitAckTask> getQueue();

    //添加任务
    void addTask(WaitAckTask task);

    //移除任务
    boolean removeTask(WaitAckTask task);

    //处理收到的已读回复
    void executeReceiveAck(BaseMessage message);

    //执行未读消息任务
    void executeWaitAckTask(WaitAckTask task);

    //合并聊天和otc事件流转通知
    int getNotice4OtcCount(String uid);
}
