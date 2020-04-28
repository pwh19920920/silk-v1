package com.spark.bitrade.controller;

import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.TradePlate;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 *  公共模块
 *
 * @author young
 * @time 2019.12.10 09:44
 */
public abstract class CommonController {

    /**
     * 从exchange实时获取盘口信息并初始化到market模块的缓存中
     *
     * @param symbol  
     * @author yangch
     * @time 2018.06.29 15:25 
     */
    protected Optional<CoinProcessor> getCoinProcessor(CoinProcessorFactory coinProcessorFactory, RestTemplate restTemplate, String symbol) {
        if (null != symbol) {
            symbol = symbol.toUpperCase();
        }
        CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
        if (coinProcessor != null && !coinProcessor.isTradePlateinitialize()) {
            //远程RPC服务URL,后缀为币种单位
            String serviceName = "SERVICE-EXCHANGE-TRADE";

            String urlBuy = "http://" + serviceName + "/extrade/monitor/realTimePlate?symbol=" + symbol + "&direction=" + ExchangeOrderDirection.BUY;
            ResponseEntity<TradePlate> responseBuy = restTemplate.getForEntity(urlBuy, TradePlate.class);
            coinProcessor.setTradePlate(responseBuy.getBody());

            String urlSell = "http://" + serviceName + "/extrade/monitor/realTimePlate?symbol=" + symbol + "&direction=" + ExchangeOrderDirection.SELL;
            ResponseEntity<TradePlate> responseSell = restTemplate.getForEntity(urlSell, TradePlate.class);
            coinProcessor.setTradePlate(responseSell.getBody());

            // 完成初始化
            coinProcessor.isTradePlateinitialize(true);
        }

        return Optional.ofNullable(coinProcessor);
    }

    protected String toUpperCase(String val) {
        if (null != val) {
            return val.toUpperCase();
        }

        return val;
    }
}
