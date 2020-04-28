package com.spark.bitrade.notice.task;

import com.spark.bitrade.entity.ChatMessageRecord;
import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/***
 * 等待确认的任务
 * @author yangch
 * @time 2018.12.21 14:12
 */
@Data
public class WaitAckTask implements Delayed {

    private String taskId;           //任务ID
    private Long delayTime;          //延迟时间
    private TimeUnit delayTimeUnit;  //延迟时间单位
    private Long executeTime;        //ms,执行时间
    private ChatMessageRecord message;

    /**
     * @param delayTime 延迟时间
     * @param delayTimeUnit 延迟时间单位
     * @param message
     */
    public WaitAckTask(long delayTime, TimeUnit delayTimeUnit, ChatMessageRecord message){
        this.delayTime = delayTime;
        this.delayTimeUnit = delayTimeUnit;
        this.executeTime = System.currentTimeMillis()
                + delayTimeUnit.toMillis(delayTime);
        this.message = message;
    }

    /**
     *
     * @param delayTime 延迟时间
     * @param delayTimeUnit 延迟时间单位
     * @param taskId 任务ID
     * @param message
     */
    public WaitAckTask(long delayTime, TimeUnit delayTimeUnit, String taskId, ChatMessageRecord message){
        this.delayTime = delayTime;
        this.delayTimeUnit = delayTimeUnit;
        this.executeTime = System.currentTimeMillis()
                + delayTimeUnit.toMillis(delayTime);
        this.taskId = taskId;
        this.message = message;
    }

    //计算当前时间到执行时间之间还有多长时间
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if(this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
            return 1;
        }else if(this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
            return -1;
        }
        return 0;
    }

    //实例 或任务ID相同就可以认为相同
    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        } else{
            if(this == obj){ //实例相同
                return true;
            } else if(this.taskId == null){ //任务ID不存在
                return false;
            } else if (obj instanceof WaitAckTask){
                WaitAckTask c = (WaitAckTask) obj;
                if(this.taskId.equals(c.taskId)){ //任务ID相同
                    return true ;
                }
            }
        }
        return false ;
    }
}
