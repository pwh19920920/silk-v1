package com.spark.bitrade.processor;

import com.spark.bitrade.entity.CoinThumb;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/***
 * 币种兑换汇率
 * @author yangch
 * @time 2018.09.19 16:49
 */
@FeignClient("bitrade-market")
public interface ICoinExchangeRate {
    /**
     *
     * @param displayArea 可以为null
     * @param keyWord 过滤关键字，可以为null
     * @return
     */
    @RequestMapping("/market/symbol-thumb")
    List<CoinThumb> findSymbolThumb(@RequestParam(value = "displayArea",required = false) String displayArea,
                                    @RequestParam(value = "keyWord",required = false) String keyWord);
}
