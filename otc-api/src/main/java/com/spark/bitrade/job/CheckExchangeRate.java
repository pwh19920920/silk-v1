package com.spark.bitrade.job;

import com.spark.bitrade.coin.CoinExchangeFactory;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@Slf4j
public class CheckExchangeRate {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CoinExchangeFactory factory;

    @Scheduled(fixedRate = 5*60*1000)
    public void syncRate(){
        factory.getCoins().forEach((symbol,value)->{
            String serviceName = "bitrade-market";
            String url = "http://" + serviceName + "/market/exchange-rate/cny/{coin}";
            ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, symbol);
            log.info("remote call:service={},result={}", serviceName, result);
            if(result.getStatusCode().value() == 200 && result.getBody().getCode() == 0){
                BigDecimal rate =  new BigDecimal((String)result.getBody().getData());
                factory.set(symbol,rate);
            }
            else {
                factory.set(symbol,BigDecimal.ZERO);
            }
        });

    }
}
