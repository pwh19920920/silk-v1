package com.spark.bitrade.feign;

import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;


/**
 * 用于和market通信
 * @author yangch
 * @time 2018.10.31 12:41
 */
@FeignClient("bitrade-market")
public interface ICoinExchange {

    @RequestMapping("/market/exchange-rate/usd/{coin}")
    MessageResult getUsdExchangeRate(@PathVariable(value = "coin", required = false) String coin);

    @RequestMapping("/market/exchange-rate/cny/{coin}")
    MessageResult getCnyExchangeRate(@PathVariable(value = "coin", required = false) String coin);

    /**
     * 获取基于cnyt的汇率，获取汇率的顺序 CNYT、USDT、BTC、ETH
     *
     * @param coin 币种简称名称，如USDT、USD、CNYT、CNY、BT、BTC、LTC...
     * @return 基于CNYT转换后汇率，无汇率则返回0
     */
    @RequestMapping("/market/exchange-rate/cnyt/{coin}")
    MessageRespResult<BigDecimal> getCnytExchangeRate(@PathVariable(value = "coin", required = false) String coin);

    @RequestMapping("/market/exchange-rate/usd-cny")
    MessageResult getUsdCnyRate();

    @RequestMapping("/market/api/v1/symbol-thumb/one")
    MessageRespResult<CoinThumb> findCoinThumbBySymol(@RequestParam("symbol") String symbol);

}
