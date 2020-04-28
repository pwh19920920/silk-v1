package com.spark.bitrade.config;

import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.component.CoinProcessorManager;
import com.spark.bitrade.consumer.ExecutorManager;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.ExchangeCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class ProcessorConfig {

    /**
     * 初始化CoinProcessorFactory和CoinProcessor
     *
     * @param coinService
     * @param exchangeRate
     * @param coinProcessorManager
     * @param executorManager
     * @return
     */
    @Bean
    public CoinProcessorFactory processorFactory(ExchangeCoinService coinService,
                                                 CoinExchangeRate exchangeRate,
                                                 CoinProcessorManager coinProcessorManager,
                                                 ExecutorManager executorManager) {
        log.info("====initialized CoinProcessorFactory start==================================");
        //初始化CoinProcessorFactory
        CoinProcessorFactory factory = new CoinProcessorFactory();
        List<ExchangeCoin> coins = coinService.findAllEnabled();

        for (ExchangeCoin coin : coins) {
            coinProcessorManager.onlineCoinProcessor(factory, coin);
        }

        //yangch：循环调用？？
        exchangeRate.setCoinProcessorFactory(factory);

        //初始化执行器
        executorManager.initExecutor(factory);
        executorManager.initExecutor();

        log.info("====initialized CoinProcessorFactory completed===============================");
        return factory;
    }


}
