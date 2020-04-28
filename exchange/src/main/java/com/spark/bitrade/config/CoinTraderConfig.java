package com.spark.bitrade.config;

import com.spark.bitrade.trader.CoinTraderFactory;
import com.spark.bitrade.consumer.ExchangeOrderCacheAndConsumer;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.mq.PlateMessageWrapper;
import com.spark.bitrade.service.ExchangeCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Slf4j
@Configuration
public class CoinTraderConfig {

    /**
     * 配置交易处理类
     *
     * @param exchangeCoinService
     * @param kafkaTemplate
     * @return
     */
    @Bean
    public CoinTraderFactory getCoinTrader(ExchangeCoinService exchangeCoinService,
                                           KafkaTemplate<String, String> kafkaTemplate,
                                           ExchangeOrderCacheAndConsumer exchangeOrderCacheAndConsumer,
                                           PlateMessageWrapper plateMessageWrapper) {
        log.info("======initialize CoinTraderFactory start======");
        CoinTraderFactory factory = new CoinTraderFactory();
        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        for (ExchangeCoin coin : coins) {
            factory.onlineTrader(coin, exchangeOrderCacheAndConsumer, kafkaTemplate, plateMessageWrapper);
        }

        log.info("======initialize CoinTraderFactory completed======");

        return factory;
    }

}
