package com.spark.bitrade.feign;

import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderStatus;
import com.spark.bitrade.constant.ExchangeOrderType;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.ExchangeWalletDto;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeOrderDetail;
import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 *  创建订单、查询订单服务接口
 *
 * @author young
 * @time 2019.09.06 18:01
 */
@FeignClient("service-exchange-v2")
public interface IExchange2Service {
    String uri_prefix = "/exchange2/service/v1/order/";

//    /**
//     * 创建订单
//     *
//     * @param order 订单
//     * @return 新增结果
//     */
//    @PostMapping(value = uri_prefix + "createOrder")
//    MessageRespResult<ExchangeOrder> createOrder(@RequestBody ExchangeOrder order);

    /**
     * 创建订单
     *
     * @param memberId     用户ID
     * @param direction    订单方向
     * @param symbol       交易对
     * @param price        委托价
     * @param amount       委托数量
     * @param type         交易类型
     * @param tradeCaptcha 交易验证码
     * @return
     */
    @PostMapping(value = uri_prefix + "place")
    MessageRespResult placeOrder(@RequestParam("memberId") Long memberId,
                                                 @RequestParam("direction") ExchangeOrderDirection direction,
                                                 @RequestParam("symbol") String symbol,
                                                 @RequestParam("price") BigDecimal price,
                                                 @RequestParam("amount") BigDecimal amount,
                                                 @RequestParam("type") ExchangeOrderType type,
                                                 @RequestParam(value = "tradeCaptcha", required = false) String tradeCaptcha);

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
     * 查询订单明细
     *
     * @param memberId 用户ID
     * @param orderId  订单号
     * @return
     */
    @PostMapping(value = uri_prefix + "queryOrderDetail")
    MessageRespResult<ExchangeOrder> queryOrderDetail(@RequestParam("memberId") Long memberId,
                                                      @RequestParam("orderId") String orderId);

    /**
     * 查询订单的撮合明细记录
     *
     * @param orderId 订单号
     * @return
     */
    @PostMapping(value = uri_prefix + "listTradeDetail")
    MessageRespResult<List<ExchangeOrderDetail>> listTradeDetail(@RequestParam("orderId") String orderId);

    /**
     * 限制重复提交
     *
     * @param orderId 订单号
     * @return true=限制访问，false=可以访问
     */
    @PostMapping(value = uri_prefix + "isRequestLimit")
    MessageRespResult<Boolean> isRequestLimit(@RequestParam("orderId") String orderId);

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
     * 分页查询交易中的订单
     *
     * @param size       分页.每页数量，不超过100
     * @param current    分页.当前页码
     * @param memberId   用户ID
     * @param symbol     交易对，eg：SLU/USDT
     * @param coinSymbol
     * @param baseSymbol
     * @param direction
     * @return
     */
    @PostMapping(value = uri_prefix + "openOrdersAndDetail")
    MessageRespResult<PageData<ExchangeOrder>> openOrdersAndDetail(@RequestParam("size") Integer size,
                                                          @RequestParam("current") Integer current,
                                                          @RequestParam("memberId") Long memberId,
                                                          @RequestParam(value = "symbol", required = false) String symbol,
                                                          @RequestParam(value = "coinSymbol", required = false) String coinSymbol,
                                                          @RequestParam(value = "baseSymbol", required = false) String baseSymbol,
                                                          @RequestParam(value = "direction", required = false) ExchangeOrderDirection direction);

    /**
     * 分页查询历史订单数据
     *
     * @param size       分页.每页数量，不超过100
     * @param current    分页.当前页码
     * @param memberId   用户ID
     * @param symbol     交易对
     * @param coinSymbol
     * @param baseSymbol
     * @param direction
     * @param status
     * @return
     */
    @PostMapping(value = uri_prefix + "historyOrdersAndDetail")
    MessageRespResult<PageData<ExchangeOrder>> historyOrdersAndDetail(@RequestParam("size") Integer size,
                                                             @RequestParam("current") Integer current,
                                                             @RequestParam("memberId") Long memberId,
                                                             @RequestParam(value = "symbol", required = false) String symbol,
                                                             @RequestParam(value = "coinSymbol", required = false) String coinSymbol,
                                                             @RequestParam(value = "baseSymbol", required = false) String baseSymbol,
                                                             @RequestParam(value = "direction", required = false) ExchangeOrderDirection direction,
                                                             @RequestParam(value = "status", required = false) ExchangeOrderStatus status);


    /**
     * 获取指定币种的可用余额
     *
     * @param memberId 用户ID
     * @param coinUnit 币种
     * @return 单条数据
     */
    @PostMapping(value = "/exchange2/service/v1/exchangeWallet")
    MessageRespResult<ExchangeWalletDto> balance(@RequestParam("memberId") Long memberId, @RequestParam("coinUnit") String coinUnit);
}
