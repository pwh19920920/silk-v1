package com.spark.bitrade.job;


import com.alibaba.fastjson.JSON;
import com.spark.bitrade.util.MessageRespResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 同步平台外第三方价格
 */
@Component
@Slf4j
@RefreshScope
public class SyncThirdPartyPriceJob implements CommandLineRunner {
    @Autowired
    private RestTemplate restTemplate;

    private boolean running = false;

    // 同步外部汇率的币种
    @Value("#{'${job.sync.symbols}'.split(',')}")
    private List<String> symbols;

    @Value("${job.sync.url:http://ticker/ticker/api/v1/exchange/currency?symbol=%s}")
    private String uri;

    /**
     * 第三方价格缓存
     */
    private Map<String, BigDecimal> cachedThirdPartyUsdPrice = new HashMap<>();

    /**
     * 同步外部汇率
     */
    //@Scheduled(cron = "10 0/3 * * * *", initialDelay = 10*60*1000)
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 3 * 60 * 1000)
    public void syncSymbolsPrice() {
        log.info("syncSymbolsPrice >>> 同步外部汇率,symbols={}", symbols);
        if (!running) {
            log.info("syncSymbolsPrice >>> 服务未启动好，暂不执行同步外部汇率任务");
        }

        try {
            for (String symbol : symbols) {
                //this.syncPrice(symbol);
                try {
                    ResponseEntity<MessageRespResult> res = restTemplate.getForEntity(String.format(uri, symbol), MessageRespResult.class);
                    log.info("result:{}", res.getBody());

                    if (res.getStatusCode() == HttpStatus.OK && res.getBody().isSuccess()) {
                        //ApiPriceData apiPriceData = (ApiPriceData) res.getBody().getData();
                        ApiPriceData apiPriceData = JSON.parseObject(JSON.toJSONString(res.getBody().getData()), ApiPriceData.class);
                        if (apiPriceData.getRate().compareTo(BigDecimal.ZERO) > 0) {
                            // 缓存价格
                            if ("CNY".equals(symbol)) {
                                cachedThirdPartyUsdPrice.put("GCNY", apiPriceData.getRate());
                            } else {
                                cachedThirdPartyUsdPrice.put(symbol, apiPriceData.getRate());
                            }
                            log.info("cached successed.{}={}", symbol, apiPriceData.getRate());
                        }
                    }
                } catch (Exception ex) {
                    log.error("同步汇率报错！", ex);
                }
            }
        } catch (Exception ex) {
            log.error("同步平台外第三方价格失败", ex);
        }
    }

//    private void syncPrice(String symbol) {
//        try {
//            ResponseEntity<MessageRespResult> res = restTemplate.getForEntity(String.format(uri, symbol), MessageRespResult.class);
//            log.info("result:{}", res.getBody());
//
//            if (res.getStatusCode() == HttpStatus.OK && res.getBody().isSuccess()) {
//                //ApiPriceData apiPriceData = (ApiPriceData) res.getBody().getData();
//                ApiPriceData apiPriceData = JSON.parseObject(JSON.toJSONString(res.getBody().getData()), ApiPriceData.class);
//                if (apiPriceData.getRate().compareTo(BigDecimal.ZERO) > 0) {
//                    // 缓存价格
//                    cachedThirdPartyUsdPrice.put(symbol, apiPriceData.getRate());
//                    log.info("cached successed.{}={}", symbol, apiPriceData.getRate());
//                }
//            }
//        } catch (Exception ex) {
//            log.error("同步汇率报错！", ex);
//        }
//    }

    public BigDecimal getThirdPartyUsdPrice(String coinUnit) {
        return cachedThirdPartyUsdPrice.get(coinUnit);
    }

    @Override
    public void run(String... strings) throws Exception {
        running = true;
    }

    @Data
    private static class ApiPriceData {

        // 转换汇率前的货币代码
        private String baseCoin;

        // 转换汇率后的货币代码
        private String symbol;

        // 汇率结果
        private BigDecimal rate;

        // 行情更新时间(10位unix时间戳)
        private Long lastUpdated;
    }

}
