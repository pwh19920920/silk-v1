package com.spark.bitrade.config;

import com.spark.bitrade.coin.CoinExchangeFactory;
import com.spark.bitrade.entity.OtcCoin;
import com.spark.bitrade.service.OtcCoinService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class CoinExchangeFactoryConfig {

    @Bean
    public CoinExchangeFactory getCoinExchangeFactory(OtcCoinService coinService){
        List<OtcCoin> coins = coinService.findAll();
        CoinExchangeFactory factory = new CoinExchangeFactory();
        coins.forEach(coin->{
            factory.set(coin.getUnit(),new BigDecimal(0));
        });
        factory.set("USDT",new BigDecimal(0)); //add by yangch 时间： 2018.04.26 原因：代码合并
        return factory;
    }
}
