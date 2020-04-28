package com.spark.bitrade.consumer;

import com.spark.bitrade.entity.ExchangeCoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/***
 * 线程池的初始化
 * @author yangch
 * @time 2018.08.21 9:42
 */

@Component
@Slf4j
public class ExecutorManager {

    @Autowired
    private HandleOrderCompletedCacheAndConsumer handleOrderCompletedCacheAndConsumer;

    @Autowired
    private ExecutorService executor;


    public void initExecutor(){
        handleOrderCompletedCacheAndConsumer.initHandleTradeCacheAndConsumer();
        log.info("=====handleOrderCompletedCacheAndConsumer initHandleTradeCacheAndConsumer() completed====");
    }

    public ExecutorService getExecutor(){
        return this.executor;
    }

    public void shutdown(){
        this.executor.shutdown();
    }
}
