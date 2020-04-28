package com.spark.bitrade.job;

import com.spark.bitrade.system.CoinExchangeFactory;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Slf4j
public class CheckExchangeRate {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CoinExchangeFactory factory;
    private String serviceName = "bitrade-market";

    @Scheduled(fixedRate = 60*1000)
    public void syncRate(){
        BigDecimal cnyRate = getUsdCnyRate();
        factory.getCoins().forEach((symbol,value)->{
            BigDecimal usdRate = getUsdRate(symbol);
            factory.set(symbol,usdRate,cnyRate.multiply(usdRate).setScale(2, BigDecimal.ROUND_HALF_UP)); //edit by tansitao 时间： 2018/11/11 原因：修改为四舍五入
        });
    }

    public BigDecimal getUsdRate(String unit){
        String url = "http://" + serviceName + "/market/exchange-rate/usd/{coin}";
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, unit);
        log.info("remote call:service={},result={}", serviceName, result);
        if(result.getStatusCode().value() == 200 && result.getBody().getCode() == 0){
            BigDecimal rate =  new BigDecimal((String)result.getBody().getData());
            return rate;
        }
        else{
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getUsdCnyRate(){
        String url = "http://" + serviceName + "/market/exchange-rate/usd-cny";
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
        log.info("remote call:service={},result={}", serviceName, result);
        if(result.getStatusCode().value() == 200 && result.getBody().getCode() == 0){
            BigDecimal rate =  new BigDecimal((Double)result.getBody().getData());
            return rate;
        }
        else{
            return BigDecimal.ZERO;
        }
    }
}
