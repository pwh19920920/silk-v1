package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.service.KafkaOffsetCacheService;
import com.spark.bitrade.service.PushTradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * 按交易对多线程处理 成交明细
 * @author yangch
 * @time 2018.08.16 9:56
 */

@Component
@Slf4j
public class HandleTradeCacheAndConsumer2 {
    @Autowired
    private PushTradeMessage pushTradeMessage;
    //用此方法会导致 Asnyc注解失效
//    @Autowired
//    private TaskExecutor taskExecutor;
//    @Autowired
//    private AsyncTaskExecutor taskExecutor;
    @Autowired
    private ExecutorService executor;


    //成交明细缓存
    private static Map<String, BlockingQueue<ConsumerRecord<String,String>>> exchangeTradeConsumerCache = new HashMap<>();
    ///private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 初始化资源
     * @param exchangeCoin
     */
    public void initHandleTradeCacheAndConsumer (ExchangeCoin exchangeCoin){
        //初始化缓存队列
        exchangeTradeConsumerCache.put(exchangeCoin.getSymbol(), new LinkedBlockingQueue());
        //初始化 消费线程
        executor.submit(new HandleTradeConsumerThread(exchangeCoin.getSymbol()));
    }


    /**
     * 添加成交明细到队列中
     * @param record
     */
    public void put2HandleTradeQueue(ConsumerRecord<String, String> record){
        try {
            BlockingQueue<ConsumerRecord<String, String>> queue = exchangeTradeConsumerCache.get(record.key());
            if (queue == null) {
                log.error("===该交易对的队列不存在,symbol={},record={}", record.key(), record.value());
                return;
            }

            queue.put(record);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



    //成交明细处理 消费线程
    public class HandleTradeConsumerThread  implements Runnable{
        private final String symbol;

        public HandleTradeConsumerThread(String symbol){
            this.symbol = symbol;
        }

        @Override
        public void run() {
            try {
                //处理成交明细
                BlockingQueue<ConsumerRecord<String, String>> queue = exchangeTradeConsumerCache.get(symbol);
                while (true) {
                    try {
                        ConsumerRecord<String, String> record = queue.take();
                        if(null == record){
                            continue;
                        }

                        log.info("symbol={},topic={},key={},value={}", symbol ,record.topic(), record.key(), record.value());
                        List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
                        if(null == trades){
                            //log.warn("成交明细为空");
                            continue;
                        }

                        /*for (ExchangeTrade trade : trades) {
                            processExchangeTradeService.asyncProcessExchangeTrade(trade, ExchangeOrderDirection.BUY, record.offset()); //处理买单明细
                            processExchangeTradeService.asyncProcessExchangeTrade(trade, ExchangeOrderDirection.SELL, record.offset()); //处理买单明细
                        }*/
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
