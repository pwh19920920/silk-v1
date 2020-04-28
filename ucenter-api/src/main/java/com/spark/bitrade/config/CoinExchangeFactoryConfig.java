package com.spark.bitrade.config;

import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.system.CoinExchangeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class CoinExchangeFactoryConfig {
    @Autowired
    private CoinService coinService;

    @Bean
    public CoinExchangeFactory createCoinExchangeFactory() {
        //edit by yangch 时间： 2018.05.10 原因：代码合并
        List<Coin> coinList = coinService.findAll();
        //List<Coin> coinList = coinService.findAllCoinByOtc();
        CoinExchangeFactory factory = new CoinExchangeFactory();
        coinList.forEach(coin ->
                factory.set(coin.getUnit(), new BigDecimal(coin.getUsdRate()), new BigDecimal(coin.getCnyRate()))
        );
        return factory;
    }
}
