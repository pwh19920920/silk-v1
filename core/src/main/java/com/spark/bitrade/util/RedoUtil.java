package com.spark.bitrade.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 * 重做工具类
 * @author Zhang Yanjun
 * @time 2018.09.28 16:48
 */
public class RedoUtil {

    Logger logger= LoggerFactory.getLogger(RedoUtil.class);

    public MessageResult redo(RestTemplate restTemplate,Long id){
        String serviceName = "bitrade-market";
        String url = "http://" + serviceName + "/market/redo?id="+id;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(15 * 1000);
        requestFactory.setReadTimeout(5 * 1000);
        restTemplate.setRequestFactory(requestFactory);
        ResponseEntity<MessageResult> pricResult = restTemplate.getForEntity(url, MessageResult.class);
        MessageResult mr = pricResult.getBody();
        MessageResult result=mr;
        logger.info("====重做后返回的结果 {}====","code:"+result.getCode()+", message:"+result.getMessage()+", data:"+result.getData());
        return result;
    }
}
