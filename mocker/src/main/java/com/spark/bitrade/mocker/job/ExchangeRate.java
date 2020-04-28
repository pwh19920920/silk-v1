package com.spark.bitrade.mocker.job;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/***
 * 获取cnyt和eth的价格
 * @author yangch
 * @time 2018.12.10 11:17
 */

@Component
@Slf4j
public class ExchangeRate {
    @Autowired
    private RestTemplate restTemplate;

    //基于usdt的价格
    private BigDecimal cnytPrice = BigDecimal.valueOf(0.1451378809869376);
    private BigDecimal usdtPrice = BigDecimal.valueOf(6.8);
    private BigDecimal ethPrice =BigDecimal.ZERO;

    @Value("${domain.silktrader}")
    private String domain;


    @Scheduled(fixedRate = 60*1000)
    public void syncRate(){
        BigDecimal cnytRate = syncRate("cnyt");
        if(cnytRate.compareTo(BigDecimal.ZERO)>0){
            cnytPrice = cnytRate;
        }

        BigDecimal ethTate = syncRate("eth");
        if(ethTate.compareTo(BigDecimal.ZERO)>0){
            ethPrice = ethTate;
        }
    }

    private BigDecimal syncRate(String unit){
        String url = domain + "/market/exchange-rate/usd/"+unit;
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, unit);
        log.info("remote call:result={}", result);
        if(result.getStatusCode().value() == 200 && result.getBody().getCode() == 0){
            BigDecimal rate =  new BigDecimal((String)result.getBody().getData());
            return rate;
        }
        else{
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCnytPrice() {
        return cnytPrice;
    }

    public BigDecimal getUsdtPrice() {
        usdtPrice = BigDecimal.ONE.divide(cnytPrice).setScale(2, BigDecimal.ROUND_DOWN);
        return usdtPrice;
    }

    public BigDecimal getEthPrice() {
        return ethPrice;
    }


    @Data
    static class MessageResult{
        private int code;
        private String message;
        private Object data;
    }

}
