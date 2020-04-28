package com.spark.bitrade.service;

import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 *  币币交易服务接口
 * 备注：market模块无法支持feign
 *
 * @author young
 * @time 2019.09.06 18:01
 * @FeignClient("service-exchange-v2")
 */
public interface IExchange2Service {
    String uri_prefix = "http://service-exchange-v2/exchange2/service/v1/order/";

    /**
     * 重做
     *
     * @param id
     * @return
     */
    MessageResult redo(@RequestParam("id") Long id);
}
