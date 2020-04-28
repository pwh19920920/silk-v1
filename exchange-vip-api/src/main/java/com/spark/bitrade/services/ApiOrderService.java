package com.spark.bitrade.services;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.vo.OrderCancelVo;
import com.spark.bitrade.controller.vo.RequestOrderVo;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeOrderDetail;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.feign.ICywService;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.ExchangeOrderDetailService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.IdWorkByTwitter;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

/**
 * <p>第三方接口订单服务</p>
 *
 * @author tian.bo
 * @date 2019-09-02 10:21:55
 */
@Service
@Slf4j
public class ApiOrderService {
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MemberService memberService;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    @Qualifier("idWorkByTwitterSnowflake")
    private IdWorkByTwitter idWorkByTwitter;

    @Autowired
    private ICywService cywService;

    /**
     * 添加委托订单
     *
     * @param member
     * @param direction
     * @param symbol
     * @param price
     * @param amount
     * @param type
     * @return
     */
    public MessageResult createOrder(AuthMember member, ExchangeOrderDirection direction,
                                     String symbol, BigDecimal price, BigDecimal amount, ExchangeOrderType type) {
        long startTime = System.currentTimeMillis();

        // 机器人交易不支持市价交易
        Assert.isTrue(type == ExchangeOrderType.LIMIT_PRICE, "机器人接口不支持市价交易");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.error(500, this.getMessage("ILLEGAL_QUANTITY"));
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0
                && type == ExchangeOrderType.LIMIT_PRICE) {
            return MessageResult.error(500, this.getMessage("ILLEGAL_PRICE"));
        }

        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        if (exchangeCoin == null || exchangeCoin.getEnable() != 1
                || exchangeCoin.getSymbol().equalsIgnoreCase(symbol) == false) {
            log.error("不支持的交易对。用户ID={}, 交易对={}, 交易对配置={}", member.getId(), symbol, exchangeCoin);
            return MessageResult.error(500, this.getMessage("UNSUPPORTED"));
        }

        //用户状态及交易状态判断
        this.checkUserStatus(member);

        //设置价格精度
        price = price.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
            return MessageResult.error(500, this.getMessage("ILLEGAL_PRICE"));
        }

        //设置数量精度
        if (direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.MARKET_PRICE) {
            amount = amount.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
            if (amount.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                return MessageResult.error(500, this.getMessage("TURNOVER_LIMIT") + exchangeCoin.getMinTurnover());
            }
        } else {
            amount = amount.setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_DOWN);
            // 最少委托数量限制
            if (direction == ExchangeOrderDirection.BUY) {
                // 买单最低限制
                if (amount.compareTo(exchangeCoin.getMinAmount()) < 0) {
                    return MessageResult.error(500, this.getMessage("NUMBER_LIMIT") + exchangeCoin.getMinAmount());
                }
            } else {
                // 卖单最低限制
                if (amount.compareTo(exchangeCoin.getMinSellAmount()) < 0) {
                    return MessageResult.error(500, this.getMessage("NUMBER_LIMIT") + exchangeCoin.getMinSellAmount());
                }
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.error(500, this.getMessage("ILLEGAL_QUANTITY"));
        }


        //如果有最低卖价限制，出价不能低于此价,且禁止市场价格卖
        if (direction == ExchangeOrderDirection.SELL && exchangeCoin.getMinSellPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMinSellPrice()) < 0) || type == ExchangeOrderType.MARKET_PRICE)) {
            return MessageResult.error(500, this.getMessage("CANNOT_LOWER") + exchangeCoin.getMinSellPrice());
        }
        //查看是否启用市价买卖
        if (type == ExchangeOrderType.MARKET_PRICE) {
            if (exchangeCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.BUY) {
                return MessageResult.error(500, this.getMessage("NOT_SUPPORT_BUY"));
            } else if (exchangeCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.SELL) {
                return MessageResult.error(500, this.getMessage("NOT_SUPPORT_SELL"));
            }
        }

        ExchangeOrder order = new ExchangeOrder();
        //生成订单ID
        order.setOrderId(this.genOrderId(member.getId(), symbol));
        order.setMemberId(member.getId());
        order.setSymbol(symbol);
        order.setBaseSymbol(exchangeCoin.getBaseSymbol());
        order.setCoinSymbol(exchangeCoin.getCoinSymbol());
        order.setPrice(price);
        order.setType(type);
        //限价买入单时amount为用户设置的总成交额
        order.setAmount(amount);
        order.setDirection(direction);
        order.setTime(Calendar.getInstance().getTimeInMillis());
        order.setStatus(ExchangeOrderStatus.TRADING);
        order.setTradedAmount(BigDecimal.ZERO);

        //计算冻结数量
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            BigDecimal turnover;
            if (order.getType() == ExchangeOrderType.MARKET_PRICE) {
                turnover = order.getAmount();
            } else {
                //设置计划交易额的精度，应该使用价格精度
                turnover = order.getAmount().multiply(order.getPrice())
                        .setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_UP);
            }
            //记录冻结数量
            order.setFreezeAmount(turnover);
        } else {
            //记录冻结数量
            order.setFreezeAmount(order.getAmount());
        }

        try {
            //edit by young 时间： 2019.09.09 原因：调用机器人接口，保存订单并冻结余额
            MessageRespResult<ExchangeOrder> result = cywService.createOrder(order);
            ///MessageResult mr = orderService.addOrder(member.getId(), order, exchangeCoin);
            if (result.isSuccess()
                    && result.getData() != null
                    && result.getData().getOrderId().equals(order.getOrderId())) {
                // 发送消息至Exchange系统
                kafkaTemplate.send("exchange-order", symbol, JSON.toJSONString(order));
            } else {
                return result.to();
                ///return MessageResult.error(500, this.getMessage("SUBMIT_FAILED"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, this.getMessage("SUBMIT_FAILED"));
        }

        log.info("下单成功！订单号={}, 下单消耗时间={}", order.getOrderId(), (System.currentTimeMillis() - startTime));
        return MessageResult.success(this.getMessage("SUBMIT_SUCCESS"), order);
    }

    /**
     * 用户状态及交易状态判断
     *
     * @param member
     */
    private void checkUserStatus(AuthMember member) {
        Member memberNow = memberService.findOne(member.getId());
        Assert.notNull(memberNow, this.getMessage("ILLEGAL_USER"));
        Assert.isTrue(memberNow.getStatus() == null
                        || memberNow.getStatus() == CommonStatus.NORMAL,
                this.getMessage("NOT_ALLOWED_TRADE"));
        Assert.isTrue(memberNow.getTransactionStatus() == null
                        || memberNow.getTransactionStatus() == BooleanEnum.IS_TRUE,
                this.getMessage("LIMIT_TRAD"));
    }


    /**
     * 批量添加订单（注意：无事务性的保障）
     *
     * @param member    会员登录信息
     * @param listOrder 订单信息集合
     * @return
     */
    public MessageResult createBatchOrders(AuthMember member, List<RequestOrderVo> listOrder) {

        if (null == listOrder) {
            return MessageResult.error(this.getMessage("ERROR_PARAM_FORMAT"));
        }

        //按订单记录处理结果
        JSONArray result = new JSONArray();
        listOrder.forEach(param -> {
            //调用下单
            MessageResult messageResult = createOrder(member, param.getDirection(),
                    param.getSymbol(), param.getPrice(), param.getAmount(), param.getType());
            //添加处理结果
            param.setResultData(messageResult);
            result.add(param);
        });
        return MessageResult.success(this.getMessage("SUBMIT_SUCCESS"), result);
    }


    /**
     * 查询一个订单详情（不提供撮合明细）
     *
     * @param member  会员信息
     * @param orderId 订单号
     * @return
     */
    public MessageResult selectOrderInfo(AuthMember member, String orderId) {
        Assert.notNull(orderId, this.getMessage("NO_ORDER_ID"));

        //edit by young 时间： 2019.09.09 原因：调用机器人接口
        MessageRespResult<ExchangeOrder> result = this.cywService.queryOrder(member.getId(), orderId);
        return new MessageResult(result.getCode(), result.getMessage(), result.getData());
    }


    /**
     * 查询当前委托订单
     *
     * @param member   会员信息
     * @param symbol   交易对
     * @param pageNo   请求开始页码，从 0 开始
     * @param pageSize 请求数量
     * @return
     */
    public PageData<ExchangeOrder> selectOpenOrders(AuthMember member, String symbol, int pageNo, int pageSize) {
        //edit by young 时间： 2019.09.09 原因：调用机器人接口，不提供撮合明细
        MessageRespResult<List<ExchangeOrder>> result =
                this.cywService.openOrders(member.getId(), symbol);

        int size = result.getData().size();
        int pages = size % pageSize == 0 ? size / pageSize : size / pageSize + 1;
        PageData<ExchangeOrder> page = new PageData<>();
        page.setTotalElements(size);
        page.setTotalPages(pages);
        page.setNumber(pageNo);
        page.setSize(pageSize);
        page.setContent(result.getData());

        return page;
    }


    /**
     * 查询历史委托订单
     *
     * @param member   会员信息
     * @param symbol   交易对
     * @param pageNo   请求开始页码，从 0 开始
     * @param pageSize 请求数量
     * @return
     */
    public PageData<ExchangeOrder> selectOrders(AuthMember member, String symbol, int pageNo, int pageSize) {
        //edit by young 时间： 2019.09.09 原因：调用机器人接口
        MessageRespResult<PageData<ExchangeOrder>> result =
                this.cywService.historyOrders2(pageSize, pageNo, symbol, member.getId());
        return result.getData();
    }


    /**
     * 撤销委托订单
     *
     * @param member  会员信息
     * @param orderId 订单号
     * @return
     */
    public MessageResult updateCancelOrder(AuthMember member, String orderId) {
        if (null == member
                || StringUtils.isEmpty(member.getId())) {
            return MessageResult.error(this.getMessage("ILLEGAL_USER"));
        }

        if (StringUtils.isEmpty(orderId)) {
            return MessageResult.error(this.getMessage("NO_ORDER_ID"));
        }

        //从 从库中获取订单信息
        ///ExchangeOrder order = orderService.findOneReadOnly(orderId);
        //MessageRespResult<ExchangeOrder> result = this.cywService.queryOrder(member.getId(), orderId);

        MessageRespResult<ExchangeOrder> result = this.cywService.claimCancelOrder(member.getId(), orderId);
        if (result.isSuccess()) {
            if (null == result.getData()) {
                return MessageResult.error(this.getMessage("NO_TRADING_ORDER"));
            }
            if (result.getData().getMemberId() != member.getId()) {
                return MessageResult.error(500, this.getMessage("OPERATE_LIMIT"));
            }
            if (result.getData().getStatus() != ExchangeOrderStatus.TRADING) {
                return MessageResult.error(500, this.getMessage("NOT_IN_TRANSACT"));
            }

            //发送消息至Exchange系统
            ///kafkaTemplate.send("exchange-order-cancel", order.getSymbol(), JSON.toJSONString(order));

            return MessageResult.success(this.getMessage("CANCEL_SUCCESS"));
        } else {
            return new MessageResult(result.getCode(), result.getMessage(), null);
        }
    }


    /**
     * 批量撤销委托订单
     *
     * @param member        会员登录信息
     * @param orderCancelVo 订单号集合["orderId1","...","orderIdn"]
     * @return
     */
    public MessageResult updateBatchCancelOrders(AuthMember member, OrderCancelVo orderCancelVo) {
        List<String> orderList = orderCancelVo.getOrderIds();
        if (CollUtil.isEmpty(orderList)) {
            return MessageResult.error(this.getMessage("NO_ORDER_ID"));
        }
        //按订单记录处理结果
        JSONObject result = new JSONObject();
        orderList.forEach(orderId -> {
            //调用订单
            MessageResult messageResult = updateCancelOrder(member, orderId);
            result.put(orderId, messageResult);
        });
        //返回数据格式参考={"code":0,"data":{"orderIdn":{"code":0,"message":"成功"},"orderId1":{"code":0,"message":"成功"},"...":{"code":0,"message":"成功"}},"message":"ok"}
        return MessageResult.success(this.getMessage("CANCEL_SUCCESS"), result);
    }

    /**
     * 查询委托订单成交明细
     *
     * @param member  会员信息
     * @param orderId 订单号
     * @return
     */
    public List<ExchangeOrderDetail> selectCurrentOrder(AuthMember member, String orderId) {
        return exchangeOrderDetailService.findAllByOrderId(orderId);
    }

    /**
     * 生成订单号
     *
     * @param memberId
     * @param symbol
     * @return
     */
    private String genOrderId(Long memberId, String symbol) {
        //eg：S1168423154092716041_SLUUSDT
        return new StringBuilder("S").append(idWorkByTwitter.nextId()).append("_").append(symbol.replace("/", "")).toString();
    }

    /**
     * 资源消息转换
     *
     * @param code
     * @return
     */
    private String getMessage(String code) {
        if (null != msService) {
            return code;
        }
        String msg = msService.getMessage(code);
        if (StringUtils.isEmpty(msg)) {
            return code;
        }
        return msg;
    }
}
