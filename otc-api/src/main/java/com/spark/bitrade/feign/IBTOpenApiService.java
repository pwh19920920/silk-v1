package com.spark.bitrade.feign;

import com.spark.bitrade.entity.ASellConfigDetailVo;
import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * @author
 * @time
 */
@FeignClient("otc-open-server")
public interface IBTOpenApiService {
  /**
   * app-资产查询
   *
   * @param memberId
   * @return com.spark.bitrade.util.MessageRespResult<com.spark.bitrade.vo.IocoPurchaseVo>
   * @author zhangYanjun
   * @time 2019.10.01 16:41
   */
  @PostMapping(
      value = "/otc-open/app/app/appAssert",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> appAssert(@RequestParam("memberId") Long memberId);

  /**
   * app-查询交易记录
   *
   * @author zhangYanjun
   * @time 2019.10.03 23:25
   * @param pageNo
   * @param pageSize
   * @param memberId
   * @param status 订单状态
   * @param type 交易类型
   * @return com.spark.bitrade.util.MessageRespResult<java.lang.Object>
   */
  @PostMapping(
      value = "/otc-open/app/app/appOrderPages",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> appOrderPages(
      @RequestParam("pageNo") Integer pageNo,
      @RequestParam("pageSize") Integer pageSize,
      @RequestParam("memberId") Long memberId,
      @RequestParam("status") Integer status,
      @RequestParam("type") Integer type);

  /**
   * app-查询正在进行的订单
   *
   * @author zhangYanjun
   * @time 2019.10.02 10:20
   * @param pageNo
   * @param pageSize
   * @param memberId
   * @return com.spark.bitrade.util.MessageRespResult<java.lang.Object>
   */
  @PostMapping(
      value = "/otc-open/app/app/appGoingOrderPages",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> appGoingOrderPages(
      @RequestParam("pageNo") Integer pageNo,
      @RequestParam("pageSize") Integer pageSize,
      @RequestParam("memberId") Long memberId);

  /**
   * app-资产明细查询
   *
   * @param memberId
   * @param size
   * @param current
   * @return com.spark.bitrade.util.MessageRespResult<java.lang.Object>
   * @author zhangYanjun
   * @time 2019.10.01 16:43
   */
  @PostMapping(
      value = "/otc-open/app/app/assertPages",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> assertPages(
      @RequestParam("pageNo") Integer pageNo,
      @RequestParam("pageSize") Integer pageSize,
      @RequestParam("memberId") Long memberId);

  /**
   * app-查询订单记录
   *
   * @author shenzucai
   * @time 2019.10.02 9:14
   * @param orderSn
   * @return true
   */
  @PostMapping(
      value = "/otc-open/app/apiOrderDetail",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> apiOrderDetail(
      @RequestParam("orderSn") String orderSn, @RequestParam("memberId") Long memberId);

  /**
   * 商家放行
   *
   * @author shenzucai
   * @time 2019.10.02 9:14
   * @param orderSn
   * @param memberId
   * @return true
   */
  @PostMapping(
      value = "/otc-open/app/releaseForV1",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> releaseForV1(
      @RequestParam("orderSn") String orderSn, @RequestParam("memberId") Long memberId);

  /**
   * app-api卖币查询
   *
   * @author zhangYanjun
   * @time 2019.10.02 17:27
   * @param memberId
   * @return com.spark.bitrade.util.MessageRespResult<java.lang.Object>
   */
  @PostMapping(
      value = "/otc-open/app/app/sellConfigDetail",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> sellConfigDetail(@RequestParam("memberId") Long memberId);

  /**
   * app-api卖币修改
   *
   * @author zhangYanjun
   * @time 2019.10.02 17:33
   * @param vo
   * @return com.spark.bitrade.util.MessageRespResult<java.lang.Object>
   */
  @PostMapping(
      value = "/otc-open/app/app/sellConfigUpdate",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> sellConfigUpdate(@RequestBody ASellConfigDetailVo vo);

  /**
   * api划转（APP -> API）
   *
   * @author zhangYanjun
   * @time 2019.10.02 20:48
   * @param amount
   * @param memberId
   * @return com.spark.bitrade.util.MessageRespResult<java.lang.Object>
   */
  @PostMapping(
      value = "/otc-open/app/app/transfor",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  MessageRespResult<Object> sellConfigUpdate(
      @RequestParam("amount") BigDecimal amount, @RequestParam("memberId") Long memberId);
}
