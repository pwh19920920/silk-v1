package com.spark.bitrade.feign;


import java.math.BigDecimal;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spark.bitrade.util.MessageRespResult;

/**
 * @description:
 * @author: ss
 * @date: 2020/3/24
 */
@FeignClient("otc-server")
public interface IOtcServerV2Service {
    /**
     * 根据用户ID获取默认法币设置
     * @param memberId
     * @return
     */
	@PostMapping(value = "/otcServerApi/api/v2/currencyManage/getMemberCurrencyUnitById")
	public String getMemberCurrenc(@RequestParam("memberId")Long memberId);
    /**
     * 根据币种获取法币价格
     * @param fSymbol 法币币种ID
     * @param tSymbol 交易币种缩写
     * @return
     */

	@PostMapping(value = "/otcServerApi/api/v2/otcCoin/getCurrencyRate")
	 public MessageRespResult getCurrencyRate(@RequestParam("fSymbol") String fSymbol,
	                                             @RequestParam("tSymbol") String tSymbol);


}
