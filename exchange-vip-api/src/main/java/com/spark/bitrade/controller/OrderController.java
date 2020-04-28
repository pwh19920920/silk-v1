//package com.spark.bitrade.controller;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.github.pagehelper.PageInfo;
//import com.spark.bitrade.constant.*;
//import com.spark.bitrade.core.PageData;
//import com.spark.bitrade.entity.*;
//import com.spark.bitrade.entity.transform.AuthMember;
//import com.spark.bitrade.exception.UnexpectedException;
//import com.spark.bitrade.service.*;
//import com.spark.bitrade.util.IdWorkByTwitter;
//import com.spark.bitrade.util.MessageResult;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.util.Assert;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.bind.annotation.SessionAttribute;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
//
///**
// * 委托订单处理类
// */
//@Slf4j
//@RestController
//@RequestMapping("/order")
//public class OrderController {
//    @Autowired
//    private ExchangeOrderService orderService;
//    @Autowired
//    private MemberWalletService walletService;
//    @Autowired
//    private ExchangeCoinService exchangeCoinService;
//    @Autowired
//    private CoinService coinService;
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    @Autowired
//    private MemberService memberService;
//    @Autowired
//    private ExchangeOrderDetailService exchangeOrderDetailService;
//
//    @Autowired
//    private LocaleMessageSourceService msService;
//
//    @Autowired
//    @Qualifier("idWorkByTwitterSnowflake")
//    private IdWorkByTwitter idWorkByTwitter;
//
//
//    /**
//     * 添加委托订单
//     *
//     * @return
//     */
//    @RequestMapping("add")
//    public MessageResult addOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, ExchangeOrderDirection direction,
//                                  String symbol, BigDecimal price, BigDecimal amount, ExchangeOrderType type) {
//        return _addOrder(member, direction, symbol, price, amount, type);
//    }
//
//    //添加订单
//    public MessageResult _addOrder(AuthMember member, ExchangeOrderDirection direction,
//                                   String symbol, BigDecimal price, BigDecimal amount, ExchangeOrderType type) {
//        long startTime = System.currentTimeMillis();
//        String testUid = String.valueOf(member.getId());
//
//        //edit by yangch 时间： 2018.05.08 原因：堵住市价交易漏掉
//        Assert.isTrue(type == ExchangeOrderType.LIMIT_PRICE, "暂不支持市价交易");
//
//        ExchangeOrder order = new ExchangeOrder();
//        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
//            return MessageResult.error(500, msService.getMessage("ILLEGAL_PRICE"));
//        }
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            return MessageResult.error(500, msService.getMessage("ILLEGAL_QUANTITY"));
//        }
//
//        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
//        if (exchangeCoin == null || exchangeCoin.getEnable() != 1
//                || exchangeCoin.getSymbol().equalsIgnoreCase(symbol) == false) {
//            log.error("不支持的交易对。交易对={},交易对配置={}", symbol, exchangeCoin);
//            return MessageResult.error(500, msService.getMessage("UNSUPPORTED"));
//        }
//
//        //add by yangch 时间： 2018.05.04 原因：用户状态及交易状态判断
//        Member memberNow = memberService.findOne(member.getId());
//        Assert.notNull(memberNow, msService.getMessage("ILLEGAL_USER"));
//        Assert.isTrue(memberNow.getStatus() == null || memberNow.getStatus() == CommonStatus.NORMAL, msService.getMessage("NOT_ALLOWED_TRADE"));
//        Assert.isTrue(memberNow.getTransactionStatus() == null || memberNow.getTransactionStatus() == BooleanEnum.IS_TRUE, msService.getMessage("LIMIT_TRAD"));
//
//        String baseCoin = exchangeCoin.getBaseSymbol();
//        String exCoin = exchangeCoin.getCoinSymbol();
//        log.info("exCoin={},baseCoin={},direction={},type={}", exCoin, baseCoin, direction, type);
//
//        Coin coin;
//        if (direction == ExchangeOrderDirection.SELL) {
//            coin = coinService.findByUnit(exCoin);
//            if (coin.getUnit().equalsIgnoreCase(exCoin) == false) {
//                log.error("币种获取有误。委托币种={},币种信息={}", exCoin, coin);
//                coin = null;
//            }
//        } else {
//            coin = coinService.findByUnit(baseCoin);
//            if (coin.getUnit().equalsIgnoreCase(baseCoin) == false) {
//                log.error("币种获取有误。委托币种={},币种信息={}", baseCoin, coin);
//                coin = null;
//            }
//        }
//        if (coin == null) {
//            return MessageResult.error(500, msService.getMessage("UNSUPPORTED"));
//        }
//
//        //设置价格精度
//        price = price.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
//        //设置数量精度
//        if (direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.MARKET_PRICE) {
//            amount = amount.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
//            if (amount.compareTo(exchangeCoin.getMinTurnover()) < 0) {
//                return MessageResult.error(500, msService.getMessage("TURNOVER_LIMIT") + exchangeCoin.getMinTurnover());
//            }
//        } else {
//            amount = amount.setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_DOWN);
//            //add by yangch 时间： 2018.05.22 原因：最少委托数量限制
//            if (amount.compareTo(exchangeCoin.minAmount()) < 0) {
//                return MessageResult.error(500, msService.getMessage("NUMBER_LIMIT") + exchangeCoin.minAmount());
//            }
//        }
//        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
//            return MessageResult.error(500, msService.getMessage("ILLEGAL_PRICE"));
//        }
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            return MessageResult.error(500, msService.getMessage("ILLEGAL_QUANTITY"));
//        }
//
//        MemberWallet wallet = walletService.findByCoinAndMemberId(coin, member.getId());
//        if (wallet == null) {
//            return MessageResult.error(500, msService.getMessage("UNSUPPORTED"));
//        }
//        if (wallet.getCoin().getUnit().equalsIgnoreCase(coin.getUnit()) == false) {
//            log.error("获取钱包账户与指定的币种不一致。目标币种={},钱包信息={}", coin, wallet);
//            return MessageResult.error(500, msService.getMessage("UNSUPPORTED"));
//        }
//
//        ////edit by tansitao 时间： 2018/4/23 原因：修改bug，实现买卖不报余额不足bug
//        if (direction == ExchangeOrderDirection.SELL) {
//            if (wallet.getBalance().compareTo(amount) < 0) {
//                return MessageResult.error(500, msService.getMessage("INSUFFICIENT_BALANCE"));
//            }
//        } else {
//            if (wallet.getBalance().compareTo(price.multiply(amount).setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN)) < 0) {
//                return MessageResult.error(500, msService.getMessage("INSUFFICIENT_BALANCE"));
//            }
//        }
//
//        if (wallet.getIsLock() == BooleanEnum.IS_TRUE) {
//            return MessageResult.error(500, msService.getMessage("WALLET_LOCKED"));
//        }
//
//        //如果有最低卖价限制，出价不能低于此价,且禁止市场价格卖
//        if (direction == ExchangeOrderDirection.SELL && exchangeCoin.getMinSellPrice().compareTo(BigDecimal.ZERO) > 0
//                //&& price.compareTo(exchangeCoin.getMinSellPrice()) < 0){ note by yangch 2018-04-26 合并
//                && ((price.compareTo(exchangeCoin.getMinSellPrice()) < 0) || type == ExchangeOrderType.MARKET_PRICE)) {
//            return MessageResult.error(500, msService.getMessage("CANNOT_LOWER") + exchangeCoin.getMinSellPrice());
//        }
//        //查看是否启用市价买卖
//        if (type == ExchangeOrderType.MARKET_PRICE) {
//            if (exchangeCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.BUY) {
//                return MessageResult.error(500, msService.getMessage("NOT_SUPPORT_BUY"));
//            } else if (exchangeCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.SELL) {
//                return MessageResult.error(500, msService.getMessage("NOT_SUPPORT_SELL"));
//            }
//        }
//
//        //限制委托数量
//        if (exchangeCoin.getMaxTradingOrder() > 0 && orderService.findCurrentTradingCount(member.getId(), symbol, direction) >= exchangeCoin.getMaxTradingOrder()) {
//            return MessageResult.error(500, msService.getMessage("MAXIMUM_LIMIT").replace("{1}", String.valueOf(exchangeCoin.getMaxTradingOrder())));
//        }
//
//        order.setMemberId(member.getId());
//        order.setSymbol(symbol);
//        order.setBaseSymbol(baseCoin);
//        order.setCoinSymbol(exCoin);
//        order.setPrice(price);
//        order.setOrderId(getOrderId(member.getId()));   //生成订单ID
//
//        //限价买入单时amount为用户设置的总成交额
//        order.setAmount(amount);
//        order.setType(type);
//        order.setDirection(direction);
//        try {
//            MessageResult mr = orderService.addOrder(member.getId(), order, exchangeCoin);
//            if (mr.getCode() != 0) {
//                return MessageResult.error(500, msService.getMessage("SUBMIT_FAILED"));
//                //return MessageResult.error(500, msService.getMessage("SUBMIT_FAILED") + mr.getMessage());//edit by tansitao 时间： 2018/5/22 原因：修改为国际化
//            }
//
//            //log.info("add------------6-------------{}={}",testUid, System.currentTimeMillis() - startTime );
//
//            // 发送消息至Exchange系统
//            kafkaTemplate.send("exchange-order", symbol, JSON.toJSONString(order));
//
//            /*ListenableFuture<SendResult<String,String>> kafkaResult = kafkaTemplate.send("exchange-order",symbol, JSON.toJSONString(order));
//            kafkaResult.addCallback(s->{},fail->{
//                try {
//                    businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__KAFKA_SEND_ORDER, order.toString(), fail.getMessage());
//                }catch (Exception e){
//                    log.error("订单消息发送失败，订单信息：{}", order);
//                }
//            });*/
//            //log.info("add------------7-------------{}={}",testUid, System.currentTimeMillis() - startTime );
//            //log.info("add------------end-------------{},{}",testUid, DateUtil.YYYYMMDDMMHHSSSSS(DateUtil.getCurrentDate()));
//        } catch (UnexpectedException uex) {
//            log.warn("余额不够，下单失败，未成功的订单信息：{}", order);
//            return MessageResult.error(500, msService.getMessage("SUBMIT_FAILED") + "," + msService.getMessage("INSUFFICIENT_BALANCE"));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return MessageResult.error(500, msService.getMessage("SUBMIT_FAILED"));
//        }
//
//        return MessageResult.success(msService.getMessage("SUBMIT_SUCCESS"), order);
//    }
//
//    @RequestMapping("history")
//    public PageData<ExchangeOrder> historyOrderReadonly(@SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol, int pageNo, int pageSize) {
//        PageInfo<ExchangeOrder> pageInfo = orderService.findHistoryReadOnly(
//                member.getId(), symbol, PageData.pageNo4PageHelper(pageNo), pageSize, null, null, null, null);
//        pageInfo.getList().forEach(exchangeOrder -> {
//            //获取交易成交详情
//            exchangeOrder.setDetail(exchangeOrderDetailService.findHistoryAllByOrderId(exchangeOrder.getOrderId()));
//        });
//
//        return PageData.toPageData(pageInfo);
//    }
//
//    //当前委托（从只读库中获取数据）
//    @RequestMapping("current")
//    public PageData<ExchangeOrder> currentOrderReadonly(@SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol, int pageNo, int pageSize) {
//        PageInfo<ExchangeOrder> pageInfo = orderService.findCurrentReadOnly(
//                member.getId(), symbol, PageData.pageNo4PageHelper(pageNo), pageSize, null, null, null, null);
//        pageInfo.getList().forEach(exchangeOrder -> {
//            //获取交易成交详情
//            BigDecimal tradedAmount = BigDecimal.ZERO;
//            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
//            exchangeOrder.setDetail(details);
//            for (ExchangeOrderDetail trade : details) {
//                //edit by yangch 时间： 2018.05.12 原因：处理市价买入的bug
//                if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE && exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
//                    //通过 数量*价格 换算为USDT显示（废弃，需要精度换算才行）
//                    //tradedAmount = tradedAmount.add(trade.getAmount().multiply(trade.getPrice()));
//                    //edit by yangch 时间： 2018.05.21 原因：使用已成交的交易额（已经换算为USDT了）
//                    if (trade.getTurnover() == null || trade.getTurnover().compareTo(BigDecimal.ZERO) <= 0) {
//                        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(exchangeOrder.getSymbol());
//                        tradedAmount = tradedAmount.add(trade.getAmount().multiply(trade.getPrice())).setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_UP);
//                    } else {
//                        tradedAmount = tradedAmount.add(trade.getTurnover());
//                    }
//                } else {
//                    tradedAmount = tradedAmount.add(trade.getAmount());
//                }
//            }
//            exchangeOrder.setTradedAmount(tradedAmount);
//        });
//
//        return PageData.toPageData(pageInfo);
//    }
//
//    /***
//      * 查询订单明细
//      * @author yangch
//      * @time 2018.07.17 15:00 
//     * @param member
//     * @param orderId
//     */
//    @RequestMapping("orderInfo")
//    public MessageResult orderDetail(@SessionAttribute(SESSION_MEMBER) AuthMember member, String orderId) {
//        Assert.notNull(orderId, msService.getMessage("NO_ORDER_ID"));
//        ExchangeOrder exchangeOrder = orderService.findOneReadOnly(orderId);
//        Assert.notNull(exchangeOrder, msService.getMessage("NO_ORDER"));
//        Assert.isTrue(exchangeOrder.getMemberId() == member.getId(), msService.getMessage("NO_ORDER"));
//
//        if (exchangeOrder.getStatus() == ExchangeOrderStatus.TRADING) {
//            exchangeOrder.setDetail(exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId()));
//        } else {
//            exchangeOrder.setDetail(exchangeOrderDetailService.findHistoryAllByOrderId(exchangeOrder.getOrderId())); //查询订单撮单明细
//        }
//
//        return MessageResult.success("SUCCESS", exchangeOrder);
//    }
//
//
//    /**
//     * 查询委托成交明细
//     *
//     * @param member
//     * @param orderId
//     * @return
//     */
//    @RequestMapping("detail/{orderId}")
//    public List<ExchangeOrderDetail> currentOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String orderId) {
//        return exchangeOrderDetailService.findAllByOrderId(orderId);
//    }
//
//    /**
//     * 撤单接口，无撤单次数限制
//     *
//     * @param member  用户登录信息
//     * @param orderId 订单号
//     * @return
//     */
//    public MessageResult _cancelOrder(AuthMember member, String orderId) {
//        if (null == member
//                || StringUtils.isEmpty(member.getId())) {
//            return MessageResult.error(msService.getMessage("ILLEGAL_USER"));
//        }
//
//        if (StringUtils.isEmpty(orderId)) {
//            return MessageResult.error(msService.getMessage("NO_ORDER_ID"));
//        }
//
//        //ExchangeOrder order =  orderService.findOne(orderId); //从主库中获取订单
//        ExchangeOrder order = orderService.findOneReadOnly(orderId);   //从 从库中获取订单信息
//        if (null == order) {
//            return MessageResult.error(msService.getMessage("NO_ORDER"));
//        }
//
//        if (order.getMemberId() != member.getId()) {
//            return MessageResult.error(500, msService.getMessage("OPERATE_LIMIT"));
//        }
//        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
//            return MessageResult.error(500, msService.getMessage("NOT_IN_TRANSACT"));
//        }
//
//        //发送消息至Exchange系统
//        kafkaTemplate.send("exchange-order-cancel", order.getSymbol(), JSON.toJSONString(order));
//
//        return MessageResult.success(msService.getMessage("CANCEL_SUCCESS"));
//    }
//
//    //edit by yangch 时间： 2018.10.07 原因：修改接口，不做撤单次数限制
//    @RequestMapping("cancel/{orderId}")
//    public MessageResult cancelOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String orderId) {
//        return _cancelOrder(member, orderId);
//    }
//
//    /***
//      * 批量撤单接口（注意：无事务性的保障）
//      * @author yangch
//      * @time 2018.10.26 14:45 
//     * @param member 用户登录信息
//     * @param orderIds 订单列表，json格式=["orderId1","...","orderIdn"]
//     */
//    @RequestMapping("batchCancel")
//    public MessageResult cancelBatchOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, String orderIds) {
//        if (StringUtils.isEmpty(orderIds)) {
//            return MessageResult.error(msService.getMessage("NO_ORDER_ID"));
//        }
//
//        //按订单记录处理结果
//        JSONObject result = new JSONObject();
//
//        JSONArray jsonArray = null;
//        try {
//            jsonArray = JSON.parseArray(orderIds);
//        } catch (Exception ex) {
//            return MessageResult.error(msService.getMessage("ERROR_PARAM_FORMAT"));
//        }
//        if (null == jsonArray) {
//            return MessageResult.error(msService.getMessage("ERROR_PARAM_FORMAT"));
//        }
//
//        jsonArray.forEach(orderId -> {
//            MessageResult messageResult = _cancelOrder(member, orderId.toString());  //调用订单
//            result.put(orderId.toString(), messageResult);
//        });
//
//        //返回数据格式参考={"code":0,"data":{"orderIdn":{"code":0,"message":"成功"},"orderId1":{"code":0,"message":"成功"},"...":{"code":0,"message":"成功"}},"message":"ok"}
//        return MessageResult.success(msService.getMessage("CANCEL_SUCCESS"), result);
//    }
//
//    /**
//     * 批量添加订单（注意：无事务性的保障）
//     *
//     * @param member         用户登录信息
//     * @param batchOrderInfo 订单信息，json格式=[{symbol:"SLU/USDT",direction:"0=BUY/1=SELL",type:"1=LIMIT_PRICE",price:0.5,amount:10},{},{}]
//     * @return
//     */
//    @RequestMapping("batchAdd")
//    public MessageResult addBatchOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, String batchOrderInfo) {
////        ExchangeOrderDirection direction,
////        String symbol, BigDecimal price, BigDecimal amount, ExchangeOrderType type
//        if (StringUtils.isEmpty(batchOrderInfo)) {
//            return MessageResult.error(msService.getMessage("NO_BATCH_ORDER_PARAM"));
//        }
//
//        List<OrderAddParam> listOrder = null;
//        //参数格式解析
//        try {
//            listOrder = JSON.parseArray(batchOrderInfo, OrderAddParam.class);
//        } catch (Exception ex) {
//            return MessageResult.error(msService.getMessage("ERROR_PARAM_FORMAT"));
//        }
//
//        if (null == listOrder) {
//            return MessageResult.error(msService.getMessage("ERROR_PARAM_FORMAT"));
//        }
//
//        //按订单记录处理结果
//        JSONArray result = new JSONArray();
//        listOrder.forEach(param -> {
//            MessageResult messageResult = _addOrder(member, param.getDirection(),
//                    param.getSymbol(), param.getPrice(), param.getAmount(), param.getType());  //调用下单
//            param.setResultData(messageResult); //添加处理结果
//            result.add(param);
//        });
//
//        return MessageResult.success(msService.getMessage("SUBMIT_SUCCESS"), result);
//    }
//
//    //获取订单ID
//    public String getOrderId(Long memberId) {
//        return new StringBuilder("E").append(memberId).append(idWorkByTwitter.nextId()).toString();
//    }
//
//    //订单下单实体类
//    @Data
//    public static class OrderAddParam {
//        private String symbol;  //交易对
//        private ExchangeOrderDirection direction;   //交易方向
//        private ExchangeOrderType type; //交易类型
//        private BigDecimal price;   //委托价格
//        private BigDecimal amount;  //委托数量
//
//        private String customContent; //自定义内容，非必须
//        private Object resultData;  //下单处理结果
//
//        @Override
//        public String toString() {
//            return JSON.toJSONString(this);
//        }
//    }
//
//}
