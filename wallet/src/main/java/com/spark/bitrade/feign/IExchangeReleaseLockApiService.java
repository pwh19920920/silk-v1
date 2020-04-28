package com.spark.bitrade.feign;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.dto.ExchangeReleaseLockRequestDTO;
import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/***
  * 充值锁仓
  * @author lc
  * @time 2019.12.17
  */
@FeignClient("service-exchange-v2-release")
public interface IExchangeReleaseLockApiService {

    /**
     *  链上充值成功后调用此方法,减少可用 增加锁仓.
     * @parm
     * @return
     */
    @RequestMapping(value = "/exchange2-release/service/v2/lock/exchangeReleaseLock", method = RequestMethod.POST)
    MessageRespResult exchangeReleaseLock(@RequestParam("exchangeReleaseLockRequestDTO") String exchangeReleaseLockRequestDTO);

}
