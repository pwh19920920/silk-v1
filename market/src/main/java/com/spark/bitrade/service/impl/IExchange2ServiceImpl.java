package com.spark.bitrade.service.impl;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.service.IExchange2Service;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

/**
 *  
 *
 * @author young
 * @time 2019.11.14 18:57
 */
@Slf4j
@Service
public class IExchange2ServiceImpl implements IExchange2Service {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public MessageResult redo(@RequestParam("id") Long id) {
        StringBuilder url = new StringBuilder(uri_prefix + "redo?");
        url.append("id=").append(id);

        ResponseEntity<MessageRespResult> res = restTemplate.getForEntity(url.toString(), MessageRespResult.class);
        if (res.getStatusCode() == HttpStatus.OK && res.getBody().isSuccess()) {
            return MessageResult.success();
        }

        return MessageResult.error("error");
    }

    /**
     * 远程调用服务接口
     *
     * @param url 调用url
     * @return
     */
    private MessageRespResult<ExchangeOrder> rpc(StringBuilder url) {
        ResponseEntity<MessageRespResult> res = restTemplate.getForEntity(url.toString(), MessageRespResult.class);
        if (res.getStatusCode() == HttpStatus.OK && res.getBody().isSuccess()) {
            return MessageRespResult.success4Data(JSON.parseObject(JSON.toJSONString(res.getBody().getData()), ExchangeOrder.class));
        }

        return MessageRespResult.error("error");
    }
}
