package com.spark.bitrade.service;

import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("service-exchange-v2")
public interface IExchange2FeignService {

    @RequestMapping("/exchange2/internal/v2/feeStats")
    MessageRespResult feignExchangeFee(@RequestParam("startTime") String start, @RequestParam("endTime") String end);

}
