package com.spark.bitrade.consumer.handle;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.KafkaOffsetCacheKeyConstant;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.consumer.CustomKafkaConsumer;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.service.KafkaOffsetCacheService;
import com.spark.bitrade.service.ProcessExchangeTradeService;
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
public class HandleTradeCacheAndConsumer {
    @Autowired
    private ProcessExchangeTradeService processExchangeTradeService;
    @Autowired
    private ExecutorService executor;
    @Autowired
    private KafkaOffsetCacheService kafkaOffsetCacheService;
    @Autowired
    private CustomKafkaConsumer customKafkaConsumer;


    /**
     * 缓存成交明细
     */
    private static Map<String, BlockingQueue<ConsumerRecord<String, String>>> exchangeTradeConsumerCache = new HashMap<>();

    /**
     * 初始化资源
     *
     * @param exchangeCoin
     */
    public void initHandleTradeCacheAndConsumer(ExchangeCoin exchangeCoin, CoinProcessor coinProcessor) {
        //初始化缓存队列
        exchangeTradeConsumerCache.put(exchangeCoin.getSymbol(), new LinkedBlockingQueue());
        //初始化 消费线程
        executor.submit(new HandleTradeConsumerThread(exchangeCoin.getSymbol(), coinProcessor));
    }

    public void recoverData(String symbol) {
        log.info("recover handleTrade data, symbol={}", symbol);
        List<ConsumerRecord<String, String>> list =
                customKafkaConsumer.getConsumerRecord("exchange-trade",
                        KafkaOffsetCacheKeyConstant.getKeyExchangeTrade(symbol, ExchangeOrderDirection.BUY),
                        KafkaOffsetCacheKeyConstant.getKeyExchangeTrade(symbol, ExchangeOrderDirection.SELL));
        if (null != list) {
            log.info("recover handleTrade data, size={}", list.size());
            try {

                for (ConsumerRecord<String, String> record : list) {
                    BlockingQueue<ConsumerRecord<String, String>> queue = exchangeTradeConsumerCache.get(record.key());
                    if (queue == null) {
                        log.error("===该交易对的队列不存在,symbol={},record={}", record.key(), record.value());
                        return;
                    }

                    queue.put(record);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getQueueCacheSize(String symbol) {
        return exchangeTradeConsumerCache.get(symbol).size();
    }

    public Map<String, Integer> getQueueCacheSize() {
        Map<String, Integer> map = new HashMap<>(exchangeTradeConsumerCache.size());
        for (Map.Entry<String, BlockingQueue<ConsumerRecord<String, String>>> entry : exchangeTradeConsumerCache.entrySet()) {
            map.put(entry.getKey(), entry.getValue().size());
        }
        return map;
    }

    /**
     * 添加成交明细到队列中
     *
     * @param record
     */
    public void put2HandleTradeQueue(ConsumerRecord<String, String> record) {
        try {
            // 在Redis中 记录待消费的消息（插入2条数据，买和卖）
            kafkaOffsetCacheService.rightPushList(
                    KafkaOffsetCacheKeyConstant.getKeyExchangeTrade(record.key(), ExchangeOrderDirection.BUY), record);
            kafkaOffsetCacheService.rightPushList(
                    KafkaOffsetCacheKeyConstant.getKeyExchangeTrade(record.key(), ExchangeOrderDirection.SELL), record);

            BlockingQueue<ConsumerRecord<String, String>> queue = exchangeTradeConsumerCache.get(record.key());
            if (queue == null) {
                //todo 待完善
                log.error("===该交易对的队列不存在,symbol={},record={}", record.key(), record.value());
                return;
            }

            queue.put(record);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 成交明细处理 消费线程
     */
    public class HandleTradeConsumerThread implements Runnable {
        private final String symbol;
        private final CoinProcessor coinProcessor;


        HandleTradeConsumerThread(String symbol, CoinProcessor coinProcessor) {
            this.symbol = symbol;
            this.coinProcessor = coinProcessor;
        }

        @Override
        public void run() {
            try {
                //处理成交明细
                BlockingQueue<ConsumerRecord<String, String>> queue = exchangeTradeConsumerCache.get(symbol);
                while (true) {
                    try {
                        ConsumerRecord<String, String> record = queue.take();

                        log.info("symbol={},value={}", symbol, record.value());
                        List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
                        if (null == trades) {
                            log.warn("成交明细为空");
                            continue;
                        }

                        for (ExchangeTrade trade : trades) {
                            //设置基币汇率
                            if (coinProcessor != null && coinProcessor.getThumb() != null) {
                                trade.setBaseUsdRate(coinProcessor.getThumb().getBaseUsdRate());
                            }

                            //分发 买单明细 任务
                            processExchangeTradeService.asyncSendExchangeTradeTask(trade, ExchangeOrderDirection.BUY, record.offset());
                            //分发 卖单明细 任务
                            processExchangeTradeService.asyncSendExchangeTradeTask(trade, ExchangeOrderDirection.SELL, record.offset());
                        }

                        // 处理K线行情
                        coinProcessor.process(trades);
                    } catch (Exception e) {
                        log.error("成交明细处理失败", e);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
