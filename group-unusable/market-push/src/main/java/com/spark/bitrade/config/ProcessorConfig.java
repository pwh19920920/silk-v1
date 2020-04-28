package com.spark.bitrade.config;

import com.spark.bitrade.cache.CoinCacheProcessor;
import com.spark.bitrade.cache.CoinCacheProcessorFactory;
import com.spark.bitrade.consumer.ExecutorManager;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.service.ExchangeCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class ProcessorConfig {

    //初始化CoinProcessorFactory和CoinProcessor
    @Bean
    public CoinCacheProcessorFactory processorFactory(
                                                ExchangeCoinService coinService,
                                                //CoinExchangeRate exchangeRate,
                                                ExecutorManager executorManager
                                                 ) {
        log.info("====initialized CoinProcessorFactory start==================================");

        CoinCacheProcessorFactory factory = new CoinCacheProcessorFactory(); //初始化CoinProcessorFactory
        List<ExchangeCoin> coins = coinService.findAllEnabled();

        for (ExchangeCoin coin : coins) {
            CoinCacheProcessor nowProcessor =factory.getProcessor(coin.getSymbol());
            if(null == nowProcessor) {
                CoinCacheProcessor processor = new CoinCacheProcessor(coin.getSymbol()); //初始化CoinProcessor
                processor.setIsHalt(true);

                factory.addProcessor(coin.getSymbol(), processor); //保存交易对和CoinProcessor的关系
                log.info("new processor completed, symbol={} ", coin.getSymbol());

                //初始化执行器，按条件初始化
//                executorManager.initExecutor(coin);
//                executorManager.initExecutor(processor);
//                executorManager.initExecutor(coin, processor);
            } else {
                log.warn("the CoinProcessor already exist, symbol={}", coin.getSymbol());
            }
        }
        //exchangeRate.setCoinProcessorFactory(factory); //yangch：循环调用？？

        //初始化执行器
        //executorManager.initExecutor(factory);
        executorManager.initExecutor();

        log.info("====initialized CoinProcessorFactory completed===============================");
        return factory;
    }


}
