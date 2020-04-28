package com.spark.bitrade;

import com.spark.bitrade.consumer.ExecutorManager;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.LatestTradeCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ApplicationEvent implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private ExchangeCoinService coinService;

    @Autowired
    private ExecutorManager executorManager;

    @Autowired
    private LatestTradeCacheService latestTradeCacheService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //初始化执行器
        executorManager.initExecutor();

        log.info("======================应用数据初始化开始=====================");

        List<ExchangeCoin> coins = coinService.findAllEnabled();
        /*CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
        if(processor.isHalt()) {
            //初始化最新的实时成交数据
            latestTradeCacheService.initCacheQueue(coin.getSymbol());
            log.info("==== initialized latestTradeCacheService.initCacheQueue({}) completed =====", coin.getSymbol());

            processor.initializeRealtimeKline(); //初始化K线数据
            processor.initializeThumb();         //从日K线中获取数据
            processor.initializeUsdRate();       //初始化 usd的汇率
            processor.setIsHalt(false);
            log.info("==== initialized CoinProcessor({}) data completed =====", coin.getSymbol());
        } else {
            log.warn("the CoinProcessor is running, symbol={}", coin.getSymbol());
        }*/


        coins.forEach(coin->{
            //初始化最新的实时成交数据
            latestTradeCacheService.initCacheQueue(coin.getSymbol());
        });

        log.info("======================应用数据初始化完成=====================\r\n\r\n\r\n");
    }
}
