package com.spark.bitrade.service;

import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/****
 * 提供 接入服务
 * @author yangch
 * @time 2018.12.18 11:07
 */
@FeignClient("service-lock")
public interface ILockService {

    /**
     *
     * @param param
     */
    @RequestMapping("/lock-api/lock/reward/redo")
    MessageRespResult rewardRedo(@RequestParam(value = "param")String param);
}
