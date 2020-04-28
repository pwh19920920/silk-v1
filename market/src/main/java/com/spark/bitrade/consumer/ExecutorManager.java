package com.spark.bitrade.consumer;

import com.spark.bitrade.consumer.handle.HandleOrderCompletedCacheAndConsumer;
import com.spark.bitrade.consumer.handle.HandleTradeCacheAndConsumer;
import com.spark.bitrade.consumer.handle.HandleTradePlateCacheAndConsumer;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * @author yangch
 * @time 2018.08.21 9:42  
 */

@Component
@Slf4j
public class ExecutorManager {
    @Autowired
    private TradeMessageDataManager tradeMessageDataManager;

    @Autowired
    private HandleTradeCacheAndConsumer handleTradeCacheAndConsumer;

    @Autowired
    private HandleTradePlateCacheAndConsumer handleTradePlateCacheAndConsumer;

    @Autowired
    private HandleOrderCompletedCacheAndConsumer handleOrderCompletedCacheAndConsumer;

    @Autowired
    private ExecutorService executor;


    public void initExecutor(ExchangeCoin exchangeCoin) {

    }

    public void initExecutor(CoinProcessor coinProcessor) {

    }

    public void initExecutor(CoinProcessorFactory coinProcessorFactory) {
        handleTradePlateCacheAndConsumer.initHandleTradeCacheAndConsumer(coinProcessorFactory);
        log.info("=====handleTradePlateCacheAndConsumer initHandleTradeCacheAndConsumer() completed====");
    }

    public void initExecutor(ExchangeCoin exchangeCoin, CoinProcessor coinProcessor) {
        //初始化 成交明细的消费线程
        handleTradeCacheAndConsumer.initHandleTradeCacheAndConsumer(exchangeCoin, coinProcessor);
        log.info("=====handleTradeCacheAndConsumer initHandleTradeConsumer({}) completed====", exchangeCoin.getSymbol());

        handleTradePlateCacheAndConsumer.initHandleTradeCacheAndConsumer(exchangeCoin, coinProcessor);
        log.info("=====handleTradePlateCacheAndConsumer initHandleTradeCacheAndConsumer({}) completed====", exchangeCoin.getSymbol());
    }

    public void initExecutor() {
        //初始化 消息推送的线程
        tradeMessageDataManager.initPushThread();
        log.info("=====tradeMessageDataManager initPushThread() completed====");

        handleOrderCompletedCacheAndConsumer.initHandleTradeCacheAndConsumer();
        log.info("=====handleOrderCompletedCacheAndConsumer initHandleTradeCacheAndConsumer() completed====");
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    public void shutdown() {
        this.executor.shutdown();
    }
}
