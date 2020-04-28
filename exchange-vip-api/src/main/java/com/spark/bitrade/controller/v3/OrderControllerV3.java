package com.spark.bitrade.controller.v3;

import com.spark.bitrade.annotation.definition.ApiRequestLimit;
import com.spark.bitrade.controller.ApiCommonController;
import com.spark.bitrade.controller.vo.OrderCancelVo;
import com.spark.bitrade.controller.vo.RequestOrderVo;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.services.ApiOrderService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>rest 行情交易api</p>
 *
 * @author tian.bo
 * @date 2018-12-4
 */
@Slf4j
@RestController
@RequestMapping("/v3/order")
public class OrderControllerV3 extends ApiCommonController {

    @Autowired
    private ApiOrderService apiOrderService;

    /**
     * 添加委托订单
     *
     * @param request HttpServletRequest
     * @param orderVo 委托订单
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @PostMapping("/orders/place")
    public MessageResult place(HttpServletRequest request,
                               @RequestBody RequestOrderVo orderVo) {

        log.info("order params={}", orderVo.toString());
        //用户信息
        AuthMember member = super.getAuthMember(request);
        return apiOrderService.createOrder(member, orderVo.getDirection(), orderVo.getSymbol(), orderVo.getPrice(), orderVo.getAmount(), orderVo.getType());
    }


    /**
     * 查询一个订单详情
     *
     * @param request HttpServletRequest
     * @param orderId 订单号
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @GetMapping("/orders/{orderId}")
    public MessageResult orderInfo(HttpServletRequest request,
                                   @PathVariable(name = "orderId") String orderId) {

        log.info("orderId={}", orderId);
        //用户信息
        AuthMember member = super.getAuthMember(request);
        return apiOrderService.selectOrderInfo(member, orderId);
    }

    /**
     * 查询当前委托订单（从只读库中获取数据）
     *
     * @param request  HttpServletRequest
     * @param symbol   交易对
     * @param pageNo   请求开始页码，从 0 开始
     * @param pageSize 请求数量
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @GetMapping("/openOrders")
    public MessageResult openOrders(HttpServletRequest request,
                                    @RequestParam(name = "symbol") String symbol,
                                    @RequestParam(name = "pageNo") int pageNo,
                                    @RequestParam(name = "pageSize") int pageSize) {

        log.info("symbol={},pageNo={},pageSize={}", symbol, pageNo, pageSize);
        //用户信息
        AuthMember member = super.getAuthMember(request);
        return MessageResult.success("SUCCESS", apiOrderService.selectOpenOrders(member, symbol, pageNo, pageSize));
    }


    /**
     * 查询历史委托订单
     *
     * @param request  HttpServletRequest
     * @param symbol   交易对
     * @param pageNo   请求开始页码，从 0 开始
     * @param pageSize 请求数量
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @GetMapping("/orders")
    public MessageResult historyOrders(HttpServletRequest request,
                                       @RequestParam(name = "symbol") String symbol,
                                       @RequestParam(name = "pageNo") int pageNo,
                                       @RequestParam(name = "pageSize") int pageSize) {

        log.info("symbol={},pageNo={},pageSize={}", symbol, pageNo, pageSize);
        //用户信息
        AuthMember member = super.getAuthMember(request);
        return MessageResult.success("SUCCESS", apiOrderService.selectOrders(member, symbol, pageNo, pageSize));
    }


    /**
     * 撤销委托订单
     *
     * @param request HttpServletRequest
     * @param orderId 订单号
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @PostMapping("/orders/{orderId}/submitcancel")
    public MessageResult cancelOrder(HttpServletRequest request,
                                     @PathVariable String orderId) {

        log.info("orderId={}", orderId);
        //用户登录信息
        AuthMember member = super.getAuthMember(request);
        return apiOrderService.updateCancelOrder(member, orderId);
    }

    /**
     * 批量撤单接口（注意：无事务性的保障）
     *
     * @param request       HttpServletRequest
     * @param orderCancelVo 订单json对象格式={"order-ids":["orderId1","...","orderIdn"]}
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @PostMapping("/orders/batchcancel")
    public MessageResult cancelBatchOrder(HttpServletRequest request,
                                          @RequestBody OrderCancelVo orderCancelVo) {

        log.info("orderIds={}", orderCancelVo);
        //用户登录信息
        AuthMember member = super.getAuthMember(request);
        return apiOrderService.updateBatchCancelOrders(member, orderCancelVo);
    }

    /**
     * 批量添加订单（注意：无事务性的保障）
     *
     * @param request   用户登录信息
     * @param orderList 订单信息，json格式=[{symbol:"SLU/USDT",direction:"0=BUY/1=SELL",type:"1=LIMIT_PRICE",price:0.5,amount:10},{},{}]
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @PostMapping("/orders/batchplace")
    public MessageResult addBatchOrder(HttpServletRequest request,
                                       @RequestBody List<RequestOrderVo> orderList) {

        log.info("batchOrderInfo={}", orderList);
        //用户登录信息
        AuthMember member = super.getAuthMember(request);
        return apiOrderService.createBatchOrders(member, orderList);
    }

    /**
     * 查询委托订单成交明细
     *
     * @param request HttpServletRequest
     * @param orderId 订单号
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @GetMapping("/orders/detail/{orderId}/matchresults")
    public MessageResult currentOrder(HttpServletRequest request, @PathVariable String orderId) {
        log.info("orderId={}", orderId);
        //用户登录信息
        AuthMember member = super.getAuthMember(request);
        return MessageResult.success("SUCCESS", apiOrderService.selectCurrentOrder(member, orderId));
    }

}
