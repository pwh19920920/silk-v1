package com.spark.bitrade.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
  * 获取币种价格的工具类
  * @author tansitao
  * @time 2018/6/26 14:29 
  */
public class PriceUtil {
    private Logger logger = LoggerFactory.getLogger(PriceUtil.class);

    /**
     * 获取币种价格
     * @author tansitao
     * @time 2018/6/26 14:28 
     */
    public BigDecimal getPriceByCoin(RestTemplate restTemplate, String unit)
    {

        //获取活动币种最新价格
        String serviceName = "bitrade-market";
        String url = "http://" + serviceName + "/market/exchange-rate/usd/" + unit;
        BigDecimal activityCoinPrice = BigDecimal.ZERO;
        try
        {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(15 * 1000);
            requestFactory.setReadTimeout(5 * 1000);
            restTemplate.setRequestFactory(requestFactory);
            ResponseEntity<MessageResult> pricResult = restTemplate.getForEntity(url, MessageResult.class);
            MessageResult mr = pricResult.getBody();
            logger.info("=========查询" + unit + "价格后返回的结果{}=========", mr.getCode()+ "===" + mr.getData());
            if (mr.getCode() == 0)
            {
                activityCoinPrice = BigDecimal.valueOf(Double.parseDouble(mr.getData().toString()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  activityCoinPrice;
    }

    /**
      * 获取USDT的人名币价格
      * @author tansitao
      * @time 2018/6/26 14:28 
      */
    public BigDecimal getUSDTPrice(RestTemplate restTemplate)
    {

        //获取活动币种最新价格
        String serviceName = "bitrade-market";
        String url = "http://" + serviceName + "/market/exchange-rate/usd-cny";
        BigDecimal activityCoinPrice = BigDecimal.ZERO;
        try
        {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(15 * 1000);
            requestFactory.setReadTimeout(5 * 1000);
            restTemplate.setRequestFactory(requestFactory);
            ResponseEntity<MessageResult> pricResult = restTemplate.getForEntity(url, MessageResult.class);
            MessageResult mr = pricResult.getBody();
            logger.info("=========查询USDT的人民币价格后返回的结果{}=========", mr.getCode()+ "===" + mr.getData());
            if (mr.getCode() == 0)
            {
                activityCoinPrice = BigDecimal.valueOf(Double.parseDouble(mr.getData().toString()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  activityCoinPrice;
    }

    /**
     * 获取币种的人民币价格
     * @author fumy
     * @time 2018.06.27 17:53
     * @param restTemplate
     * @param unit
     * @return true
     */
    public BigDecimal getCoinCnyPrice(RestTemplate restTemplate,String unit)
    {

        //获取活动币种最新价格
        String serviceName = "bitrade-market";
        String url = "http://" + serviceName + "/market/exchange-rate/cny/" + unit;
        BigDecimal activityCoinPrice = BigDecimal.ZERO;
        try
        {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(15 * 1000);
            requestFactory.setReadTimeout(5 * 1000);
            restTemplate.setRequestFactory(requestFactory);
            ResponseEntity<MessageResult> pricResult = restTemplate.getForEntity(url, MessageResult.class);
            MessageResult mr = pricResult.getBody();
            logger.info("=========查询"+unit+"的人民币价格后返回的结果{}=========", mr.getCode()+ "===" + mr.getMessage());
            if (mr.getCode() == 0)
            {
                activityCoinPrice = BigDecimal.valueOf(Double.parseDouble(mr.getData().toString()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  activityCoinPrice;
    }

    /**
     * 币种转化汇率
     * @author tansitao
     * @time 2018/10/31 16:36 
     * @return 汇率
     */
    public static BigDecimal toRate( int CoinScale ,BigDecimal sourcePrice, BigDecimal targetPrice){
        return sourcePrice.divide(targetPrice, CoinScale + 1, BigDecimal.ROUND_UP);
    }

    /**
     * 币种汇率转化
     * @author tansitao
     * @time 2018/10/31 16:36 
     * @param sourceAmount 转化前的数目
     */
    public static BigDecimal toRate(BigDecimal sourceAmount,int coinScale, BigDecimal sourcePrice, BigDecimal targetPrice) {
        return toRate(coinScale, sourcePrice, targetPrice).multiply(sourceAmount).setScale(coinScale, BigDecimal.ROUND_UP);
    }
}
