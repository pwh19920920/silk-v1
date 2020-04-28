package com.spark.bitrade.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.spark.bitrade.component.CoinExchangeRate;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/***
  * 同步CoinExchangeRate类中usdCnyRate属性的汇率
 *
  * @author yangch
  * @time 2019.04.09 15:56
  */
@Component
@Slf4j
@RefreshScope
public class SyncUsdToCnyPriceJob {

    /**
     * forex接口的key
     */
    @Value("${forex.api.key:RXF3lkWvD5sNKHiCLTNUPx2bj1eCtJ2M}")
    private String forexKey;

    /**
     * 对从forex获取的价格进行校正
     */
    @Value("${forex.adjust.price:0.2}")
    private double forexAdjustPrice;


    /**
     * 火币Otc接口域名
     */
    @Value("${domain.api.huobi.otc:https://otc-api.eiijo.cn}")
    private String domainOtcHuobi;

    /**
     * 是否启用拉取火币otc的均价
     */
    @Value("${domain.api.huobi.otc.enable:true}")
    private boolean enableDomainOtcHuobi;

    @Autowired
    private CoinExchangeRate coinExchangeRate;


    /**
     * 每小时同步一次价格，获取外部的USD汇率
     *
     * @throws UnirestException
     */
    @Scheduled(cron = "0 0/3 * * * *")
    public void syncPrice() throws UnirestException {
        //从火币同步场外同步数据，如果同步失败则从forex同步
        try {
            if (!syncPriceFromOtcHuobi()) {
                syncPriceFromForex();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            syncPriceFromForex();
        }
    }

    /**
     * 从火币Otc同步价格
     *
     * @return
     * @throws UnirestException
     */
    private boolean syncPriceFromOtcHuobi() throws UnirestException {
        if (!enableDomainOtcHuobi) {
            log.info("未启用从火币Otc同步价格的接口");
            return false;
        }

        String url = domainOtcHuobi + "/v1/data/config/purchase-price?coinId=2&currencyId=1";
        HttpResponse<JsonNode> resp = Unirest.get(url).asJson();
        log.info("otc.huobi result:{}", resp.getBody());

        JSONObject resultObj = resp.getBody().getObject();
        if (resultObj != null && resultObj.getBoolean("success")) {
            JSONObject priceObj = resultObj.getJSONObject("data");
            if (priceObj != null && priceObj.getDouble("price") > 0) {
                coinExchangeRate.setUsdCnyRate(new BigDecimal(priceObj.getDouble("price")).setScale(2, RoundingMode.HALF_UP));
                return true;
            }
        }

        return false;
    }

    /**
     * 从forex同步价格
     *
     * @throws UnirestException
     */
    private void syncPriceFromForex() throws UnirestException {
        String url = "https://forex.1forge.com/1.0.2/quotes";
        HttpResponse<JsonNode> resp = Unirest.get(url)
                .queryString("pairs", "USDCNH,CNHUSD")
                .queryString("api_key", forexKey)
                .asJson();
        log.info("forex result:{}", resp.getBody());
        JSONArray result = JSON.parseArray(resp.getBody().toString());
        result.forEach(json -> {
            com.alibaba.fastjson.JSONObject obj = (com.alibaba.fastjson.JSONObject) json;
            if (obj.getString("symbol").equals("USDCNH")) {
                coinExchangeRate.setUsdCnyRate(new BigDecimal(obj.getDouble("price")).setScale(2, RoundingMode.HALF_UP)
                        .add(new BigDecimal(forexAdjustPrice)));
                log.debug(obj.toString());
            }
            //del by yangch 时间： 2018.05.03 原因：代码合并
            /*else if(obj.getString("symbol").equals("CNHUSD")){
                setCnyUsdRate(new BigDecimal(obj.getDouble("price")).setScale(2,RoundingMode.DOWN));
            }*/
        });
    }
}