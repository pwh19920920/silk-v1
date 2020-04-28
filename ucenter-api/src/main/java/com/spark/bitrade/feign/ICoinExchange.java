package com.spark.bitrade.feign;

import com.spark.bitrade.util.MessageResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 用于和market通信
 * @author shenzucai
 * @time 2018.10.31 12:41
 */
@FeignClient("bitrade-market")
public interface ICoinExchange {

    @RequestMapping("/market/exchange-rate/usd/{coin}")
    MessageResult getUsdExchangeRate(@PathVariable(value = "coin",required = false) String coin);

    @RequestMapping("/market/exchange-rate/cny/{coin}")
    MessageResult getCnyExchangeRate(@PathVariable(value = "coin",required = false) String coin);

    @RequestMapping("/market/exchange-rate/usd-cny")
    MessageResult getUsdCnyRate();
}
