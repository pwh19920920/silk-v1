package com.spark.bitrade.notice;

import com.spark.bitrade.notice.task.WaitAckTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/***
 * 等待确认的延迟任务服务
 *
 * @author yangch
 * @time 2018.12.21 17:08
 */
@Component
@Slf4j
public class WaitAckTaskJob implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private IWaitAckTaskService iWaitAckTaskService;

    private ExecutorService executor = Executors.newCachedThreadPool();
    private WaitAckTaskThread waitAckTaskThread;

    //初始化延迟确认的线程
    private synchronized void initRunDelayJob(){
        if(null == waitAckTaskThread) {
            log.info("WaitAckTaskThread线程不存在，初始化该线程.....");
            waitAckTaskThread = new WaitAckTaskThread(iWaitAckTaskService.getQueue());
            executor.submit(waitAckTaskThread);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("onApplicationEvent：开始初始化.........");
        initRunDelayJob();
        log.info("onApplicationEvent：完成初始化.........");
    }

    public class WaitAckTaskThread implements Runnable{
        private DelayQueue<WaitAckTask> queue;
        public WaitAckTaskThread(DelayQueue<WaitAckTask> queue){
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    WaitAckTask task = queue.take();
                    iWaitAckTaskService.executeWaitAckTask(task);
                } catch (Exception ex) {
                    log.error("运行错误", ex);
                }
            }
        }
    }
}
