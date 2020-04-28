package com.spark.bitrade.feign;

import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 *  机器人创建订单、查询订单服务接口
 *
 * @author young
 * @time 2019.09.06 18:01
 */
@FeignClient("service-exchange-cyw")
public interface ICywService {
    String uri_prefix = "/ex_cyw/service/v1/cywOrder/";

    /**
     * 创建订单
     *
     * @param order 订单
     * @return 新增结果
     */
    @PostMapping(value = uri_prefix + "createOrder")
    MessageRespResult<ExchangeOrder> createOrder(@RequestBody ExchangeOrder order);

    /**
     * 查询订单
     *
     * @param memberId 用户ID
     * @param orderId  订单号
     * @return
     */
    @PostMapping(value = uri_prefix + "queryOrder")
    MessageRespResult<ExchangeOrder> queryOrder(@RequestParam("memberId") Long memberId,
                                                @RequestParam("orderId") String orderId);

    /**
     * 申请撤销申请
     *
     * @param memberId 用户ID
     * @param orderId  订单号
     * @return
     */
    @PostMapping(value = uri_prefix + "claimCancelOrder")
    MessageRespResult<ExchangeOrder> claimCancelOrder(@RequestParam("memberId") Long memberId, @RequestParam("orderId") String orderId);

    /**
     * 查询正在交易的订单
     *
     * @param memberId 用户ID
     * @param symbol   交易对，eg：SLU/USDT
     * @return
     */
    @PostMapping(value = uri_prefix + "openOrders")
    MessageRespResult<List<ExchangeOrder>> openOrders(@RequestParam("memberId") Long memberId,
                                                      @RequestParam("symbol") String symbol);

    /**
     * 分页查询历史订单数据
     *
     * @param size     分页.每页数量，不超过100
     * @param current  分页.当前页码
     * @param symbol   交易对
     * @param memberId 用户ID
     * @return
     */
    @PostMapping(value = uri_prefix + "historyOrders2")
    MessageRespResult<PageData<ExchangeOrder>> historyOrders2(@RequestParam("size") Integer size,
                                                              @RequestParam("current") Integer current,
                                                              @RequestParam("symbol") String symbol,
                                                              @RequestParam("memberId") Long memberId);


    /**
     * 获取指定币种的可用余额
     *
     * @param memberId 用户ID
     * @param coinUnit 币种
     * @return 单条数据
     */
    @PostMapping(value = "/ex_cyw/service/v1/cywWallet/balance")
    MessageRespResult<BigDecimal> balance(@RequestParam("memberId") Long memberId, @RequestParam("coinUnit") String coinUnit);
}
