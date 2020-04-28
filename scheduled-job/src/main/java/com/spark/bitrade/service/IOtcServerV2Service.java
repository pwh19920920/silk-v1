package com.spark.bitrade.service;


import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * @description:
 * @author: ss
 * @date: 2020/3/24
 */
@FeignClient("otc-server")
public interface IOtcServerV2Service {

    /**
     * 根据币种获取法币价格
     * @param fSymbol 法币币种ID
     * @param tSymbol 交易币种缩写
     * @return
     */

    @PostMapping(
            value = "/otcServerApi/api/v2/otcCoin/getCurrencyPrice",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    MessageRespResult<BigDecimal> getCurrencyRate(@RequestParam("fSymbol") Long fSymbol,
                                                  @RequestParam("tSymbol") String tSymbol);

}
