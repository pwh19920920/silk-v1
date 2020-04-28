package com.spark.bitrade;

import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
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
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private ExchangeCoinService coinService;

    @Autowired
    private LatestTradeCacheService latestTradeCacheService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("======================应用数据初始化开始=====================");

        log.info("====开始初始化CoinProcessor====");
        List<ExchangeCoin> coins = coinService.findAllEnabled();
        coins.forEach(coin->{
            recoverCoinTraderData(coin);
        });
        log.info("====完成初始化CoinProcessor====");

        log.info("======================应用数据初始化完成=====================\r\n\r\n\r\n");
    }

    //恢复交易数据
    public void recoverCoinTraderData(ExchangeCoin coin){
        CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
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
        }
    }
}
