package com.spark.bitrade.service.impl;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.service.ICywService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 *  
 *
 * @author young
 * @time 2019.09.07 17:54
 */
@Slf4j
@Service
public class ICywServiceImpl implements ICywService {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public MessageRespResult<ExchangeOrder> tradeBuy(ExchangeTrade trade) {
        ResponseEntity<MessageRespResult> res = restTemplate.postForEntity(uri_prefix + "tradeBuy", trade, MessageRespResult.class);
        if (res.getStatusCode() == HttpStatus.OK && res.getBody().isSuccess()) {
            return MessageRespResult.success4Data(JSON.parseObject(JSON.toJSONString(res.getBody().getData()), ExchangeOrder.class));
        }

        return MessageRespResult.error("error");
    }

    @Override
    public MessageRespResult<ExchangeOrder> tradeSell(ExchangeTrade trade) {
        ResponseEntity<MessageRespResult> res = restTemplate.postForEntity(uri_prefix + "tradeSell", trade, MessageRespResult.class);
        if (res.getStatusCode() == HttpStatus.OK && res.getBody().isSuccess()) {
            return MessageRespResult.success4Data(JSON.parseObject(JSON.toJSONString(res.getBody().getData()), ExchangeOrder.class));
        }

        return MessageRespResult.error("error");
    }

    @Override
    public MessageRespResult<ExchangeOrder> completedOrder(Long memberId, String orderId,
                                                           BigDecimal tradedAmount, BigDecimal turnover) {
        StringBuilder url = new StringBuilder(uri_prefix + "completedOrder?");
        url.append("memberId=").append(memberId);
        url.append("&orderId=").append(orderId);
        url.append("&tradedAmount=").append(tradedAmount);
        url.append("&turnover=").append(turnover);


        return rpc(url);
    }

    @Override
    public MessageRespResult<ExchangeOrder> canceledOrder(Long memberId, String orderId,
                                                          BigDecimal tradedAmount, BigDecimal turnover) {
        StringBuilder url = new StringBuilder(uri_prefix + "canceledOrder?");
        url.append("memberId=").append(memberId);
        url.append("&orderId=").append(orderId);
        url.append("&tradedAmount=").append(tradedAmount);
        url.append("&turnover=").append(turnover);

        return rpc(url);
    }

    @Override
    public MessageRespResult<ExchangeOrder> canceledOrder(Long memberId, String orderId) {
        StringBuilder url = new StringBuilder(uri_prefix + "canceledOrder2?");
        url.append("memberId=").append(memberId);
        url.append("&orderId=").append(orderId);

        return rpc(url);
    }

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
