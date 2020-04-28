package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.service.PushTradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * 缓存及消费撤销订单消息
 * @author yangch
 * @time 2018.08.21 9:01
 */

@Component
@Slf4j
public class HandleOrderCanceledCacheAndConsumer {
    @Autowired
    private ExecutorService executor;
    @Autowired
    private PushTradeMessage pushTradeMessage;

    private BlockingQueue<ConsumerRecord<String, String>> queueCache;

    public HandleOrderCanceledCacheAndConsumer(){
        queueCache = new LinkedBlockingQueue();
    }

    public void initHandleTradeCacheAndConsumer (){
        executor.submit(new HandleOrderCanceledConsumerThread());
    }

    /**
     * 添加到队列中
     * @param record
     */
    public void put2HandleOrderCanceledQueue(ConsumerRecord<String, String> record){
        try {
            queueCache.put(record);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //成交明细处理 消费线程
    public class HandleOrderCanceledConsumerThread  implements Runnable{
        @Override
        public void run() {
            while (true) {
                try {
                    ConsumerRecord<String, String> record = queueCache.take();
                    if(null == record){
                        continue;
                    }

                    //异步推送撤销订单的消息
                    ExchangeOrder exchangeOrder = JSON.parseObject(record.value(), ExchangeOrder.class);
                    pushTradeMessage.pushOrderCanceled4Socket(exchangeOrder);
                    pushTradeMessage.pushOrderCanceled4Netty(exchangeOrder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
