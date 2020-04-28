package com.spark.bitrade;

import com.spark.bitrade.consumer.handle.HandleOrderCompletedCacheAndConsumer;
import com.spark.bitrade.consumer.handle.HandleTradeCacheAndConsumer;
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
    @Autowired
    private HandleTradeCacheAndConsumer handleTradeCacheAndConsumer;
    @Autowired
    private HandleOrderCompletedCacheAndConsumer handleOrderCompletedCacheAndConsumer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("======================应用数据初始化开始=====================");

        log.info("====开始初始化CoinProcessor====");
        List<ExchangeCoin> coins = coinService.findAllEnabled();
        coins.forEach(coin -> {
            this.resumeCoinTrader(coin);
            //恢复数据
            handleTradeCacheAndConsumer.recoverData(coin.getSymbol());
            handleOrderCompletedCacheAndConsumer.recoverData(coin.getSymbol());
        });
        log.info("====完成初始化CoinProcessor====");

        log.info("======================应用数据初始化完成=====================\r\n\r\n\r\n");
    }

    /**
     * 恢复交易
     *
     * @param coin
     */
    public void resumeCoinTrader(ExchangeCoin coin) {
        CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
        if (processor.isHalt()) {
            // 初始化最新的实时成交数据
            latestTradeCacheService.initCacheQueue(coin.getSymbol());
            log.info("==== initialized latestTradeCacheService.initCacheQueue({}) completed =====", coin.getSymbol());

            processor.initialize();

            log.info("==== initialized CoinProcessor({}) data completed =====", coin.getSymbol());
        } else {
            log.warn("the CoinProcessor is running, symbol={}", coin.getSymbol());
        }
    }
}
