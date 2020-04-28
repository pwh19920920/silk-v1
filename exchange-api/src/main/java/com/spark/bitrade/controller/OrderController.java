package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderStatus;
import com.spark.bitrade.constant.ExchangeOrderType;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeOrderDetail;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.feign.IExchange2Service;
import com.spark.bitrade.service.ExchangeMemberDiscountRuleService;
import com.spark.bitrade.service.ExchangeOrderService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.ValidateOpenTranscationService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 委托订单处理类
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private ExchangeOrderService orderService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${exchange.max-cancel-times:-1}")
    private int maxCancelTimes; //当日撤单次数限制(按交易对限制)

    //edit by tansitao 时间： 2018/5/22 原因：增加国际化service
    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private ExchangeMemberDiscountRuleService exchangeMemberDiscountRuleService;
    @Autowired
    private ValidateOpenTranscationService validateOpenTranscationService;


    @Autowired
    private IExchange2Service exchange2Service;

    /**
     * 添加委托订单
     *
     * @return
     */
    @RequestMapping(value = "add", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageRespResult<ExchangeOrder> addOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, ExchangeOrderDirection direction,
                                                     String symbol, BigDecimal price, BigDecimal amount, ExchangeOrderType type,
                                                     @RequestParam(value = "tradeCaptcha", required = false) String tradeCaptcha, HttpServletRequest request) {

        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
            return MessageRespResult.error(500, msService.getMessage("ILLEGAL_PRICE"));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageRespResult.error(500, msService.getMessage("ILLEGAL_QUANTITY"));
        }

        return this.formatMessageRespResult(exchange2Service.placeOrder(member.getId(), direction, symbol, price, amount, type, tradeCaptcha));
    }

    @RequestMapping(value = "history", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public PageData<ExchangeOrder> historyOrderReadonly(@SessionAttribute(SESSION_MEMBER) AuthMember member,
                                                        @RequestParam(value = "symbol", required = false) String symbol,
                                                        int pageNo, int pageSize,
                                                        @RequestParam(value = "coinSymbol", required = false) String coinSymbol,
                                                        @RequestParam(value = "baseSymbol", required = false) String baseSymbol,
                                                        @RequestParam(value = "direction", required = false) ExchangeOrderDirection direction,
                                                        @RequestParam(value = "status", required = false) ExchangeOrderStatus status) {
        return exchange2Service.historyOrdersAndDetail(pageSize, pageNo, member.getId(), symbol, coinSymbol, baseSymbol, direction, status).getData();
    }

    //当前委托
    @RequestMapping(value = "current", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public PageData<ExchangeOrder> currentOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member,
                                                @RequestParam(value = "symbol", required = false) String symbol,
                                                int pageNo, int pageSize,
                                                @RequestParam(value = "coinSymbol", required = false) String coinSymbol,
                                                @RequestParam(value = "baseSymbol", required = false) String baseSymbol,
                                                @RequestParam(value = "direction", required = false) ExchangeOrderDirection direction,
                                                @RequestParam(value = "status", required = false) ExchangeOrderStatus status) {

        return exchange2Service.openOrdersAndDetail(pageSize, pageNo, member.getId(), symbol, coinSymbol, baseSymbol, direction).getData();
    }

    /***
      * 查询订单明细
      * @author yangch
      * @time 2018.07.17 15:00 
     * @param member
     * @param orderId
     */
    @RequestMapping(value = "orderInfo", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageRespResult<ExchangeOrder> orderDetail(@SessionAttribute(SESSION_MEMBER) AuthMember member, String orderId) {
        return exchange2Service.queryOrderDetail(member.getId(), orderId);
    }


    /**
     * 查询委托成交明细
     *
     * @param member
     * @param orderId
     * @return
     */
    @RequestMapping(value = "detail/{orderId}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public List<ExchangeOrderDetail> currentOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String orderId) {
        ///return exchangeOrderDetailService.findAllByOrderId(orderId);

        return exchange2Service.listTradeDetail(orderId).getData();
    }

    @RequestMapping(value = "cancel/{orderId}",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})

    public MessageRespResult cancelOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String orderId) {
        /// exchange2Service.claimCancelOrder(member.getId(), orderId);

        MessageRespResult<Boolean> isRequestLimit = exchange2Service.isRequestLimit(orderId);
        if (isRequestLimit.getCode() != 0 || isRequestLimit.getData()) {
            isRequestLimit.setCode(4010);
            // 撤销申请已提交  FORBID_RESUBMIT
            isRequestLimit.setMessage(msService.getMessage("SUBMITED"));
            return isRequestLimit;
        }

        MessageRespResult<ExchangeOrder> resultQueryOrder = exchange2Service.queryOrder(member.getId(), orderId);
        if (!resultQueryOrder.isSuccess()) {
            return MessageRespResult.error(resultQueryOrder.getCode(), resultQueryOrder.getMessage());
        }

        if (resultQueryOrder.getData().getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageRespResult.error(500, msService.getMessage("NOT_IN_TRANSACT"));
        }
        if (maxCancelTimes > 0) {
            // 按天计算撤单次数
            if (this.inWhiteList(member.getId()) == false
                    && overMaxCancelTimes(member.getId(), resultQueryOrder.getData().getSymbol())) {
                return MessageRespResult.error(500,
                        msService.getMessage("MAXIMUM_CANCEL").replace("{1}", String.valueOf(maxCancelTimes)));
            }
        }

        this.getService().pushOrderCancelMessage(resultQueryOrder, member, orderId);

        return MessageRespResult.success(msService.getMessage("CANCEL_SUCCESS"));
    }

    /**
     * 推送撤单消息
     *
     * @param resultQueryOrder
     * @param member
     * @param orderId
     */
    @CollectActionEvent(collectType = CollectActionEventType.EXCHANGE_CANCEL_ORDER, memberId = "#member.getId()", refId = "#orderId")
    public void pushOrderCancelMessage(MessageRespResult<ExchangeOrder> resultQueryOrder, AuthMember member, String orderId) {
        // 发送消息至Exchange系统
        kafkaTemplate.send("exchange-order-cancel", resultQueryOrder.getData().getSymbol(), JSON.toJSONString(resultQueryOrder.getData()));
    }

    /**
     * 下单、撤单白名单判断
     *
     * @param uid
     * @return
     */
    private boolean inWhiteList(long uid) {
        return exchangeMemberDiscountRuleService.exist(uid);
    }

    /**
     * 判断当天是否允许撤单
     *
     * @param uid    用户ID
     * @param symbol 撤单次数
     * @return true=不允许/false=允许
     */
    private boolean overMaxCancelTimes(long uid, String symbol) {
        if (getService().cacheOverMaxCancelTimes(uid, false)) {
            return true;
        }

        if (orderService.findTodayOrderCancelTimes(uid, symbol) >= maxCancelTimes) {
            //清理缓存
            getService().evictOverMaxCancelTimes(uid);
            //缓存数据
            getService().cacheOverMaxCancelTimes(uid, true);
            return true;
        }

        return false;
    }

    /**
     * 缓存不能撤单的用户
     *
     * @param uid 用户ID
     * @return
     */
    @Cacheable(cacheNames = "exOrder", key = "'entity:CancelOrder:'+#uid")
    public boolean cacheOverMaxCancelTimes(long uid, boolean flag) {
        return flag;
    }

    /**
     * 清理缓存
     *
     * @param uid
     */
    @CacheEvict(cacheNames = "exOrder", key = "'entity:CancelOrder:'+#uid")
    public void evictOverMaxCancelTimes(long uid) {
    }

    /**
     * 手工刷新 撤销限制的白名单
     * 刷新会员优惠规则缓存(取消撤销次数的限制)，访问地址： /exchange/order/flushMemberDiscountRuleCache?memberId=0
     *
     * @param memberId
     * @return
     */
    @RequestMapping(value = "flushMemberDiscountRuleCache",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageResult flushMemberDiscountRuleCache(@RequestParam(value = "memberId", required = false) Long memberId) {
        if (null == memberId) {
            exchangeMemberDiscountRuleService.flushCache();
        } else {
            exchangeMemberDiscountRuleService.flushCache(memberId);
        }

        return MessageResult.success("更新成功");
    }

    /**
     * 获取会员优惠规则缓存(取消撤销次数的限制)
     * 访问地址： /exchange/order/getMemberDiscountRuleCache?memberId=0
     *
     * @param memberId
     * @return
     */
    @RequestMapping(value = "getMemberDiscountRuleCache",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageResult getMemberDiscountRuleCache(@RequestParam(value = "memberId", required = false) Long memberId) {
        if (null == memberId) {
            return MessageResult.success("缓存数据", exchangeMemberDiscountRuleService.getMapDiscountRuleCache());
        } else {
            return MessageResult.success("缓存数据", exchangeMemberDiscountRuleService.getMapDiscountRuleCache(memberId));
        }
    }

    public OrderController getService() {
        return SpringContextUtil.getBean(OrderController.class);
    }


    /**
     * 内容格式，根据返回的data数据进行消息的格式
     *
     * @param result
     * @return
     */
    private MessageRespResult formatMessageRespResult(MessageRespResult result) {
        String msg = this.msService.getMessage(String.valueOf(result.getCode()));
        if (StringUtils.isEmpty(msg)) {
            msg = result.getMessage();
        }

        if (result.getCode() != 0 && result.getData() != null) {
            if (result.getData() instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) result.getData();
                msg = MessageFormat.format(msg, jsonArray.toArray());
            } else {
                msg = MessageFormat.format(msg, result.getData());
            }
        }
        result.setMessage(msg);
        return result;
    }
}
