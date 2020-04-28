package com.spark.bitrade.consumer.handle;

import com.spark.bitrade.config.KafkaOffsetCacheKeyConstant;
import com.spark.bitrade.consumer.CustomKafkaConsumer;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.KafkaOffsetCacheService;
import com.spark.bitrade.service.ProcessOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 缓存及分发完成订单消息
 *
 * @author yangch
 * @time 2018.08.21 9:01  
 */

@Component
@Slf4j
public class HandleOrderCompletedCacheAndConsumer {
    @Autowired
    private ExecutorService executor;

    @Autowired
    private ProcessOrderService processOrderService;
    @Autowired
    private KafkaOffsetCacheService kafkaOffsetCacheService;
    @Autowired
    private CustomKafkaConsumer customKafkaConsumer;
    @Autowired
    private ExchangeCoinService coinService;


    private BlockingQueue<ConsumerRecord<String, String>> queueCache;

    public HandleOrderCompletedCacheAndConsumer() {
        queueCache = new LinkedBlockingQueue();
    }

    public void initHandleTradeCacheAndConsumer() {
        executor.submit(new HandleOrderCompletedConsumerThread());
    }

    public int getQueueCacheSize() {
        return queueCache.size();
    }

    public void recoverData(String symbol) {
        log.info("recover handleOrderCompleted data, symbol={}", symbol);
        List<ConsumerRecord<String, String>> list =
                customKafkaConsumer.getConsumerRecord("exchange-order-completed",
                        KafkaOffsetCacheKeyConstant.getKeyExchangeOrderCompleted(symbol));
        if (null != list) {
            log.info("recover handleOrderCompleted data, size={}", list.size());
            try {
                for (ConsumerRecord<String, String> record : list) {
                    queueCache.put(record);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void recoverData() {
        List<ExchangeCoin> coins = coinService.findAllEnabled();
        coins.forEach(coin -> recoverData(coin.getSymbol()));
    }

    /**
     * 添加到队列中
     *
     * @param record
     */
    public void put2HandleOrderCompletedQueue(ConsumerRecord<String, String> record) {
        try {
            //Redis系统中记录待消费的消息
            kafkaOffsetCacheService.rightPushList(
                    KafkaOffsetCacheKeyConstant.getKeyExchangeOrderCompleted(record.key()), record);

            queueCache.put(record);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 消费线程 分发完成订单消息
     */
    public class HandleOrderCompletedConsumerThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    ConsumerRecord<String, String> record = queueCache.take();
                    if (null == record) {
                        continue;
                    }

                    // 异步消费并分发完成订单的消息
                    processOrderService.asyncProcessOrderComplated(record);
                } catch (Exception e) {
                    log.error("订单状态处理失败", e);
                    e.printStackTrace();
                }
            }
        }
    }

}
