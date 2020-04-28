package com.spark.bitrade.feign;

import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * @author shenzucai
 * @time 2019.07.03 21:38
 */
@FeignClient("service-lock-slp")
public interface IiocoService {

    @PostMapping(value = "/lock-slp/iocoMemberTransaction/purchaseIndex")
    MessageRespResult<Object> getPurchaseData(@RequestParam("memberId") Long memberId);


    @PostMapping(value = "/lock-slp/iocoMemberTransaction/giftIndex")
    MessageRespResult<Object> giftIndex(@RequestParam("memberId") Long memberId);

    @PostMapping(value = "/lock-slp/iocoMemberTransaction/purchaseSLP")
    MessageRespResult<Boolean> purchaseSLP(@RequestParam("memberId") Long memberId, @RequestParam("purchasetUnit") String purchasetUnit
            , @RequestParam("purchaseAmount") BigDecimal purchaseAmount
            , @RequestParam("slpAmount") BigDecimal slpAmount
            , @RequestParam("share") Integer share
            , @RequestParam("activityId") Long activityId);

    @PostMapping(value = "/lock-slp/iocoMemberTransaction/giftSLP")
    MessageRespResult<Boolean> giftSLP(@RequestParam("memberId") Long memberId, @RequestParam("giftUnit") String giftUnit
            , @RequestParam("giftAmount") BigDecimal giftAmount
            , @RequestParam("giftTo") String giftTo);

    @PostMapping(value = "/lock-slp/iocoMemberTransaction/listByType")
    MessageRespResult<Object> listByType(@RequestParam("memberId") Long memberId
            , @RequestParam("size") Integer size
            , @RequestParam("current") Integer current
            , @RequestParam("type") Integer type);
}
