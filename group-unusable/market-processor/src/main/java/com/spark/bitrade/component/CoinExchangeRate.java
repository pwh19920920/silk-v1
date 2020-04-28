package com.spark.bitrade.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.spark.bitrade.constant.KafkaTopicConstant;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.ExchangeCoinService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 币种汇率管理
 */
@Component
@Slf4j
@ToString
public class CoinExchangeRate {
    @Value("${forex.api.key:RXF3lkWvD5sNKHiCLTNUPx2bj1eCtJ2M}")
    private String forexKey;
    @Getter
    @Setter
    private BigDecimal usdCnyRate = new BigDecimal("6.85");
    /*@Setter
    @Getter
    private BigDecimal cnyUsdRate = new BigDecimal(1/6.30);*/

    @Setter
    private CoinProcessorFactory coinProcessorFactory;

    @Autowired
    private CoinService coinService;
    @Autowired
    private ExchangeCoinService exCoinService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    //del by yangch 时间： 2018.04.29 原因：合并
    /*public  void setUsdCnyRate(BigDecimal rate){
        this.usdCnyRate = rate;
    }

    //del by yangch 时间： 2018.04.29 原因：合并
    public BigDecimal getUsdCnyRate() {
        return usdCnyRate;
    }*/

    public BigDecimal getUsdRate(String coinUint) {
        log.debug("CoinExchangeRate getUsdRate unit = " + coinUint);
        if (coinUint.equalsIgnoreCase("USDT")) {
            log.debug("CoinExchangeRate getUsdRate unit = USDT  ,result = ONE");
            return BigDecimal.ONE;
        } else if (coinUint.equalsIgnoreCase("CNY")) {
            log.debug("CoinExchangeRate getUsdRate unit = CNY  ,result : 1 divide {}", this.usdCnyRate);
            return BigDecimal.ONE.divide(usdCnyRate, BigDecimal.ROUND_DOWN).setScale(4, BigDecimal.ROUND_DOWN);
        } else if (coinUint.equalsIgnoreCase("CNYT")) {
            log.debug("CoinExchangeRate getUsdRate unit = CNYT  ,result : 1 divide {}", this.usdCnyRate);
            //return BigDecimal.ONE.divide(usdCnyRate, BigDecimal.ROUND_UP).setScale(4, BigDecimal.ROUND_UP);
            return BigDecimal.ONE.divide(usdCnyRate, 8, BigDecimal.ROUND_UP);
        }

        if (coinProcessorFactory != null) {
            String usdtSymbol = coinUint.toUpperCase() + "/USDT";
            if (exCoinService.isSupported(usdtSymbol)) {
                log.debug("Support exchange coin = {}", usdtSymbol);
                return getBaseRate(usdtSymbol);
            }

            //注：实际情况很难进入该段代码
            String btcSymbol = coinUint.toUpperCase() + "/BTC";
            String ethSymbol = coinUint.toUpperCase() + "/ETH";
            if (exCoinService.isSupported(ethSymbol)) {
                log.debug("Support exchange coin = {}/ETH", ethSymbol);
                return getBaseRate(ethSymbol);
            } else if (exCoinService.isSupported(btcSymbol)) {
                log.debug("Support exchange coin = {}/BTC", btcSymbol);
                return getBaseRate(btcSymbol);
            } else {
                return getDefaultUsdRate(coinUint);
            }

            /*if (exCoinService.isSupported(usdtSymbol)) {
                log.debug("Support exchange coin = {}", usdtSymbol);
                CoinProcessor processor = coinProcessorFactory.getProcessor(usdtSymbol);
                CoinThumb thumb = processor.getThumb();
                return thumb.getUsdRate();
            } else if (exCoinService.isSupported(btcSymbol)) {
                log.debug("Support exchange coin = {}/BTC", btcSymbol);
                CoinProcessor processor = coinProcessorFactory.getProcessor(btcSymbol);
                CoinThumb thumb = processor.getThumb();
                return thumb.getUsdRate();
            } else if (exCoinService.isSupported(ethSymbol)) {
                log.debug("Support exchange coin = {}/ETH", ethSymbol);
                CoinProcessor processor = coinProcessorFactory.getProcessor(ethSymbol);
                CoinThumb thumb = processor.getThumb();
                return thumb.getUsdRate();
            } else {
                return getDefaultUsdRate(symbol);
            }*/
        } else {
            return getDefaultUsdRate(coinUint);
        }
    }

    /**
     * 获取基于交易对的价格
     *
     * @param symbol 交易对
     * @return
     */
    public BigDecimal getBaseRate(String symbol){
        if (coinProcessorFactory != null) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(symbol);
            if (null != processor) {
                CoinThumb thumb = processor.getThumb();
                if (null != thumb) {
                    return thumb.getUsdRate();
                } else {
                    log.warn("行情为null，symbol={}", symbol);
                }
            } else {
                log.warn("交易对不存在，symbol={}", symbol);
            }
        }

        if(symbol!=null && symbol.endsWith("/USDT")){
            return getDefaultUsdRate(symbol.replace("/USDT",""));
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取币种设置里的默认价格
     *
     * @param symbol
     * @return
     */
    public BigDecimal getDefaultUsdRate(String symbol) {
        Coin coin = coinService.findByUnit(symbol);
        if (coin != null) {
            return new BigDecimal(coin.getUsdRate());
        } else return BigDecimal.ZERO;
    }

    public BigDecimal getCnyRate(String symbol) {
        if (symbol.equalsIgnoreCase("CNY")) {
            return BigDecimal.ONE;
        }
        return getUsdRate(symbol).multiply(usdCnyRate).setScale(2, RoundingMode.DOWN);
    }

    /**
     * 每小时同步一次价格
     *
     * @throws UnirestException
     */
    @Scheduled(cron = "0 0/3 * * * *")
    //@Scheduled(cron = "0 0 * * * *")
    public void syncPrice() throws UnirestException {
        String url = "https://forex.1forge.com/1.0.2/quotes";
        HttpResponse<JsonNode> resp = Unirest.get(url)
                .queryString("pairs", "USDCNH,CNHUSD")
                .queryString("api_key", forexKey)
                .asJson();
        log.info("forex result:{}", resp.getBody());
        JSONArray result = JSON.parseArray(resp.getBody().toString());
        result.forEach(json -> {
            JSONObject obj = (JSONObject) json;
            if (obj.getString("symbol").equals("USDCNH")) {
                BigDecimal price = new BigDecimal(obj.getDouble("price")).setScale(2, RoundingMode.DOWN);
                if(getUsdCnyRate().compareTo(price) != 0) {
                    setUsdCnyRate(price);

                    //推送变更usd价格
                    kafkaTemplate.send(KafkaTopicConstant.MSG_UPDATE_USD_PRICE, "USD", String.valueOf(price));
                }
                log.debug(obj.toString());
            }
            //del by yangch 时间： 2018.05.03 原因：代码合并
            /*else if(obj.getString("symbol").equals("CNHUSD")){
                setCnyUsdRate(new BigDecimal(obj.getDouble("price")).setScale(2,RoundingMode.DOWN));
            }*/
        });
    }
}
