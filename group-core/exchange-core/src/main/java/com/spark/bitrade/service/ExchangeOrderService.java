package com.spark.bitrade.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.ExchangeOrderDetailRepository;
import com.spark.bitrade.dao.ExchangeOrderRepository;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.mapper.dao.ExchangeOrderMapper;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.GeneratorUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class ExchangeOrderService extends BaseService {

    @Autowired
    private ExchangeOrderRepository exchangeOrderRepository;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private ExchangeOrderDetailRepository exchangeOrderDetailRepository;
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    //private OrderDetailAggregationRepository orderDetailAggregationRepository;
            OrderDetailAggregationService orderDetailAggregationService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;

    @Autowired
    private ExchangeOrderMapper exchangeOrderMapper;

    @Autowired
    private PromoteRewardService promoteRewardService;
    @Autowired
    private PartnerRewardService partnerRewardService;

    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;
    @Autowired
    private ExchangeMemberDiscountRuleService exchangeMemberDiscountRuleService;


    public Page<ExchangeOrder> findAll(Predicate predicate, Pageable pageable) {
        return exchangeOrderRepository.findAll(predicate, pageable);
    }


    /**
     * 添加委托订单
     *
     * @param memberId
     * @param order
     * @return
     */
    //@Transactional
    public MessageResult addOrder(Long memberId, ExchangeOrder order, ExchangeCoin exchangeCoin)
            throws UnexpectedException {
        order.setTime(Calendar.getInstance().getTimeInMillis());
        order.setStatus(ExchangeOrderStatus.TRADING);
        order.setTradedAmount(BigDecimal.ZERO);
        if (StringUtils.isEmpty(order.getOrderId())) {
            order.setOrderId(GeneratorUtil.getOrderId("E") + memberId);
        }
        log.info("add order:{}", order);


        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberIdReadOnly(order.getBaseSymbol(), memberId);
            //MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getBaseSymbol(), memberId);
            BigDecimal turnover;
            if (order.getType() == ExchangeOrderType.MARKET_PRICE) {
                turnover = order.getAmount();
            } else {
                //edit by yangch 时间： 2018.05.23 原因：设置计划交易额的精度
                //turnover = order.getAmount().multiply(order.getPrice());

                //edit by yangch 时间： 2018.09.29 原因：精度修改，应该使用价格精度
                ///turnover = order.getAmount().multiply(order.getPrice()).setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_UP);
                turnover = order.getAmount().multiply(order.getPrice()).setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_UP);
            }
            order.setFreezeAmount(turnover); //记录冻结数量

            return getService().saveOrder4Transactional(order, wallet, turnover);

//            if (wallet.getBalance().compareTo(turnover) < 0) {
//                return MessageResult.error(500, "insufficient coin:" + order.getBaseSymbol());
//            } else {
//                order.setFreezeAmount(turnover); //记录冻结数量
                /*order = exchangeOrderRepository.saveAndFlush(order); //保存订单
                if (null == order) {
                    return MessageResult.error(500, "error");
                }

                MessageResult mr = memberWalletService.freezeBalance(wallet, turnover);
                if(mr.getCode() != 0){
                    return mr;
                }*/

//            }
        } else if (order.getDirection() == ExchangeOrderDirection.SELL) {
            MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberIdReadOnly(order.getCoinSymbol(), memberId);
            order.setFreezeAmount(order.getAmount()); //记录冻结数量

            return getService().saveOrder4Transactional(order, wallet, order.getAmount());

            //MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getCoinSymbol(), memberId);
//            if (wallet.getBalance().compareTo(order.getAmount()) < 0) {
//                return MessageResult.error(500, "insufficient coin:" + order.getCoinSymbol());
//            } else {
//                order.setFreezeAmount(order.getAmount()); //记录冻结数量
                /*order = exchangeOrderRepository.saveAndFlush(order); //保存订单
                if (null == order) {
                    return MessageResult.error(500, "error");
                }

                MessageResult mr = memberWalletService.freezeBalance(wallet, order.getAmount());
                if(mr.getCode() != 0){
                    return mr;
                }*/
//            }
        } else {
            log.warn("订单类型错误,订单信息：{}", order);
            return MessageResult.error(500, "订单类型错误");
        }
        /*order = exchangeOrderRepository.saveAndFlush(order);
        if (order != null) {
            return MessageResult.success("success");
        } else return MessageResult.error(500, "error");*/
        //return MessageResult.success("success");
    }

    //保存订单（拆分事务）
    @Transactional(rollbackFor = UnexpectedException.class)
    public MessageResult saveOrder4Transactional(final ExchangeOrder order, final MemberWallet wallet, final BigDecimal freezeAmount)
            throws UnexpectedException {
        ExchangeOrder orderNew = exchangeOrderRepository.saveAndFlush(order); //保存订单
        if (null == orderNew) {
            return MessageResult.error(500, "error");
        }

        MessageResult mr = memberWalletService.freezeBalance(wallet, freezeAmount); //冻结余额
        if (mr.getCode() != 0) {
            throw new UnexpectedException("INSUFFICIENT_BALANCE");
        }
        return mr;
    }

    /**
     * 查询历史订单（查询只读库）
     *
     * @param uid
     * @param pageNo
     * @param pageSize
     * @param coinSymbol 交易币种
     * @param baseSymbol 基币
     * @param direction  交易类型
     * @param status     委托状态
     * @return
     */
    @ReadDataSource
    public PageInfo<ExchangeOrder> findHistoryReadOnly(Long uid, String symbol,
                                                       int pageNo, int pageSize,
                                                       String coinSymbol,
                                                       String baseSymbol,
                                                       ExchangeOrderDirection direction,
                                                       ExchangeOrderStatus status) {
        com.github.pagehelper.Page<ExchangeOrder> page = PageHelper.startPage(pageNo, pageSize);
        //PageHelper会自动拦截到下面这查询sql
        this.exchangeOrderMapper.queryHistory(symbol, uid, coinSymbol, baseSymbol,
                direction == null ? null : direction.ordinal(), status == null ? null : status.ordinal());
        return page.toPageInfo();
    }

    /**
     * 查询所有订单
     *
     * @param uid      必填，用户ID
     * @param symbol   选填，交易对
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return
     */
    public PageInfo<ExchangeOrder> queryAll(Long uid, String symbol, Integer pageNo, Integer pageSize) {
        com.github.pagehelper.Page<ExchangeOrder> page = PageHelper.startPage(pageNo, pageSize);
        //PageHelper会自动拦截到下面这查询sql
        this.exchangeOrderMapper.queryAll(symbol, uid);
        return page.toPageInfo();
    }


    /**
     * @param uid
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page<ExchangeOrder> findHistory(Long uid, String symbol, int pageNo, int pageSize) {
        Sort orders = new Sort(new Sort.Order(Sort.Direction.DESC, "time"));
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, true));
        specification.add(Restrictions.eq("memberId", uid, true));
        specification.add(Restrictions.ne("status", ExchangeOrderStatus.TRADING, false));
        return exchangeOrderRepository.findAll(specification, pageRequest);
    }

    /**
     * 查询当前交易中的委托
     *
     * @param uid
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page<ExchangeOrder> findCurrent(Long uid, String symbol, int pageNo, int pageSize) {
        Sort orders = new Sort(new Sort.Order(Sort.Direction.DESC, "time"));
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, true));
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        return exchangeOrderRepository.findAll(specification, pageRequest);
    }

    /**
     * 查询当前交易中的委托（查询只读库）
     *
     * @param uid
     * @param pageNo
     * @param pageSize
     * @param coinSymbol 交易币种
     * @param baseSymbol 基币
     * @param direction  交易类型
     * @param status     委托状态
     * @return
     */
    @ReadDataSource
    public PageInfo<ExchangeOrder> findCurrentReadOnly(Long uid, String symbol,
                                                       int pageNo, int pageSize,
                                                       String coinSymbol,
                                                       String baseSymbol,
                                                       ExchangeOrderDirection direction,
                                                       ExchangeOrderStatus status) {
        com.github.pagehelper.Page<ExchangeOrder> page = PageHelper.startPage(pageNo, pageSize);
        //PageHelper会自动拦截到下面这查询sql
        this.exchangeOrderMapper.queryCurrent(symbol, uid, coinSymbol, baseSymbol,
                direction == null ? null : direction.ordinal(),
                status == null ? null : status.ordinal());
        return page.toPageInfo();
    }


    /**
     * 处理交易匹配
     *
     * @param trade
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult processExchangeTrade(ExchangeTrade trade) throws Exception {
        log.info("processExchangeTrade,trade = {}", trade);
        if (trade == null || trade.getBuyOrderId() == null || trade.getSellOrderId() == null)
            return MessageResult.error(500, "trade is null");
        ExchangeOrder buyOrder = exchangeOrderRepository.findByOrderId(trade.getBuyOrderId());
        ExchangeOrder sellOrder = exchangeOrderRepository.findByOrderId(trade.getSellOrderId());
        if (buyOrder == null || sellOrder == null) {
            log.error("order not found");
            return MessageResult.error(500, "order not found");
        }

        // 如何防止接口被重复调用 通过 processOrder 接口防止该接口被重复调用
        //del by yangch 时间： 2018.04.29 原因：代码合并
        /*if (buyOrder.getStatus() != ExchangeOrderStatus.TRADING || sellOrder.getStatus() != ExchangeOrderStatus.TRADING) {
            log.error("invalid order status");
            return MessageResult.error(500, "invalid order status");
        }*/
        //del by yangch 时间： 2018.05.10 原因：代码合并，解决市价问题
        /*if (buyOrder.getAmount().subtract(buyOrder.getTradedAmount()).compareTo(trade.getAmount()) < 0) {
            log.error("exceed available buy order amount");
            return MessageResult.error(500, "exceed available buy order amount");
        }
        if (sellOrder.getAmount().subtract(sellOrder.getTradedAmount()).compareTo(trade.getAmount()) < 0) {
            return MessageResult.error(500, "exceed available sell order amount");
        }*/
        //获取手续费率
        ExchangeCoin coin = exchangeCoinService.findBySymbol(buyOrder.getSymbol());
        if (coin == null) {
            log.error("invalid trade symbol {}", buyOrder.getSymbol());
            return MessageResult.error(500, "invalid trade symbol {}" + buyOrder.getSymbol());
        }

        //处理买入订单
        //processOrder(buyOrder, trade, coin.getFee());
        processOrder(buyOrder, trade, coin);
        //处理卖出订单
        processOrder(sellOrder, trade, coin);
        return MessageResult.success("process success");
    }

    //add by yangch 时间： 2018.08.08 原因：拆分买单和卖单的事务处理逻辑

    /**
     * 处理交易匹配（按买单和卖单拆分处理）
     *
     * @param trade     匹配订单
     * @param direction 订单类型
     * @return
     * @throws Exception
     */
    //@Transactional(rollbackFor = Exception.class)
    @Deprecated
    public MessageResult processExchangeTrade(final ExchangeTrade trade, ExchangeOrderDirection direction) throws Exception {
        log.info("direction={},trade = {}", direction.toString(), trade);
        if (trade == null) {
            return MessageResult.error(500, "匹配订单为空");
        }
        if (trade.getBuyOrderId() == null || trade.getSellOrderId() == null) {
            return MessageResult.error(500, "订单号为空");
        }

        ExchangeOrder order;
        if (ExchangeOrderDirection.BUY == direction) {
            order = exchangeOrderRepository.findByOrderId(trade.getBuyOrderId());
            ///refOrder = exchangeOrderRepository.findByOrderId(trade.getSellOrderId());
        } else if (ExchangeOrderDirection.SELL == direction) {
            order = exchangeOrderRepository.findByOrderId(trade.getSellOrderId());
            ///refOrder = exchangeOrderRepository.findByOrderId(trade.getBuyOrderId());
        } else {
            return MessageResult.error(500, "无效的订单类型");
        }

        if (order == null) {
            log.error("订单不存在");
            return MessageResult.error(500, "订单不存在");
        }
        if (!order.getSymbol().equalsIgnoreCase(trade.getSymbol())) {
            log.error("交易对不匹配，成交订单无效，需手工处理。order={}, 成交明细={}", order, trade);
            return MessageResult.error(500, "交易对不匹配，成交订单无效。订单ID="
                    + order.getOrderId() + ", 交易对=" + trade.getSymbol());
        }

        // 如何防止接口被重复调用 通过 processOrder 接口防止该接口被重复调用
        //del by yangch 时间： 2018.04.29 原因：代码合并
        /*if (buyOrder.getStatus() != ExchangeOrderStatus.TRADING || sellOrder.getStatus() != ExchangeOrderStatus.TRADING) {
            log.error("invalid order status");
            return MessageResult.error(500, "invalid order status");
        }*/
        //del by yangch 时间： 2018.05.10 原因：代码合并，解决市价问题
        /*if (buyOrder.getAmount().subtract(buyOrder.getTradedAmount()).compareTo(trade.getAmount()) < 0) {
            log.error("exceed available buy order amount");
            return MessageResult.error(500, "exceed available buy order amount");
        }
        if (sellOrder.getAmount().subtract(sellOrder.getTradedAmount()).compareTo(trade.getAmount()) < 0) {
            return MessageResult.error(500, "exceed available sell order amount");
        }*/

        //获取手续费率
        ExchangeCoin coin = exchangeCoinService.findBySymbol(order.getSymbol());
        if (coin == null) {
            log.error("invalid trade symbol {}", order.getSymbol());
            return MessageResult.error(500, "invalid trade symbol " + order.getSymbol());
        }

        //处理订单
        getService().processOrder(order, trade, coin);

        return MessageResult.success("process success", order);
    }


    /**
     * 重新处理交易匹配
     *
     * @param trade 撮单信息
     * @return
     * @throws Exception
     */
    //@Transactional(rollbackFor = Exception.class)
    public MessageResult redoProcessExchangeTrade(ExchangeTrade trade) throws Exception {
        /*//删除mongodb中的ExchangeOrderDetail文档记录
        exchangeOrderDetailService.deleteByOrderIdAndRefOrderId(trade.getBuyOrderId(), trade.getSellOrderId()); //买单记录
        exchangeOrderDetailService.deleteByOrderIdAndRefOrderId(trade.getSellOrderId(), trade.getBuyOrderId()); //卖的记录

        //删除mongodb中的OrderDetailAggregation文档记录
        orderDetailAggregationService.deleteByOrderIdAndRefOrderId(trade.getBuyOrderId(), trade.getSellOrderId()); //买单记录
        orderDetailAggregationService.deleteByOrderIdAndRefOrderId(trade.getSellOrderId(), trade.getBuyOrderId()); //卖的记录*/

        return processExchangeTrade(trade);
    }

    /**
     * 对发生交易的委托处理相应的钱包
     *
     * @param order        委托订单
     * @param trade        交易详情
     *                     //@param feeRatio 交易手续费率
     * @param exchangeCoin 交易对
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(final ExchangeOrder order
            , final ExchangeTrade trade, final ExchangeCoin exchangeCoin) throws Exception {
        Long time = Calendar.getInstance().getTimeInMillis();
        BigDecimal feeRatio = exchangeCoin.getFee(); //交易手续费率
        String refOrderId = trade.getSellOrderId();  //关联订单号
        if (order.getOrderId().equalsIgnoreCase(refOrderId)) {
            refOrderId = trade.getBuyOrderId();
        }

        //add by yangch 时间： 2018.06.05 原因：判断交易明细是否已处理
        if (exchangeOrderDetailService.existsByOrderIdAndRefOrderId(order.getOrderId(), refOrderId)) {
            log.warn("订单已处理，不能重复处理.trade={}", trade);
            return;
        }

        //成交额，收入币数量，支出币数量， 手续费，优惠的手续费（买入订单收取coin,卖出订单收取baseCoin）
        BigDecimal turnover, incomeCoinAmount, outcomeCoinAmount, fee, feeDiscount;
        String incomeSymbol, outcomeSymbol;  //收入币，支出币
        //String incomeSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getCoinSymbol() : order.getBaseSymbol();
        //String outcomeSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getBaseSymbol() : order.getCoinSymbol();
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            turnover = trade.getBuyTurnover();  //成交额
            fee = trade.getAmount().multiply(feeRatio).setScale(8, BigDecimal.ROUND_DOWN); //手续费，买入时扣交易币

            //计算 买币优惠的手续费
            if (exchangeCoin.getFeeBuyDiscount().compareTo(BigDecimal.ONE) >= 0) {
                feeDiscount = fee; //手续费全部优惠
            } else {
                feeDiscount = fee.multiply(exchangeCoin.getFeeBuyDiscount()); //买币优惠手续费数量

                BigDecimal remainingFee = fee.subtract(feeDiscount); //优惠后的当前手续费
                //计算 当前会员可优惠手续费数量
                BigDecimal memberFeeDiscount = remainingFee.multiply(
                        exchangeMemberDiscountRuleService.getDiscountRule(order.getMemberId(),
                                order.getSymbol()).getBuyDiscount());

                feeDiscount = feeDiscount.add(memberFeeDiscount).setScale(8, BigDecimal.ROUND_DOWN);
            }

            incomeSymbol = order.getCoinSymbol(); //买入时获得交易币
            incomeCoinAmount = trade.getAmount().subtract(fee.subtract(feeDiscount)); //增加可用的币，买入的时候获得交易币

            outcomeSymbol = order.getBaseSymbol();//买入时用 基币 支付
            outcomeCoinAmount = turnover; //扣除支付的币，买入的时候算成交额（基本）
        } else {
            turnover = trade.getSellTurnover(); //成交额
            fee = turnover.multiply(feeRatio).setScale(8, BigDecimal.ROUND_DOWN);  //手续费，买入时扣基币

            //计算 卖币优惠的手续费
            if (exchangeCoin.getFeeSellDiscount().compareTo(BigDecimal.ONE) >= 0) {
                feeDiscount = fee; //手续费全部优惠
            } else {
                feeDiscount = fee.multiply(exchangeCoin.getFeeSellDiscount()); //卖币优惠手续费数量
                BigDecimal remainingFee = fee.subtract(feeDiscount); //优惠后的当前手续费
                //计算 当前会员可优惠手续费数量
                BigDecimal memberFeeDiscount = remainingFee.multiply(
                        exchangeMemberDiscountRuleService.getDiscountRule(order.getMemberId(),
                                order.getSymbol()).getSellDiscount());

                feeDiscount = feeDiscount.add(memberFeeDiscount).setScale(8, BigDecimal.ROUND_DOWN);
            }

            incomeSymbol = order.getBaseSymbol(); //卖出时获得基币
            incomeCoinAmount = turnover.subtract(fee.subtract(feeDiscount)); //增加可用的币,卖出的时候获得基币

            outcomeSymbol = order.getCoinSymbol(); //买入时用 交易币 支付
            outcomeCoinAmount = trade.getAmount(); //扣除支付的币，卖出的算成交量（交易币）
        }

        Member member = memberService.findOne(order.getMemberId());
        if (member.getId().longValue() != order.getMemberId().longValue()) {
            throw new Exception(MessageFormat.format("获取的会员信息和订单的会员信息不一致。" +
                    "会员信息={0}，订单信息={1}", member, order));
        }

        //添加成交详情(mongodb库，需要先生成成交明细，防止接口被重复调用)
        ExchangeOrderDetail orderDetail = new ExchangeOrderDetail();
        orderDetail.setOrderId(order.getOrderId());
        orderDetail.setTime(time);
        orderDetail.setPrice(trade.getPrice());
        orderDetail.setAmount(trade.getAmount());
        //添加关联订单号，“订单号+关联订单号” 是唯一的
        orderDetail.setRefOrderId(refOrderId);
        orderDetail.setTurnover(turnover);
        //手续费优惠
        orderDetail.setFee(fee.subtract(feeDiscount));
        //添加优惠数量
        orderDetail.setFeeDiscount(feeDiscount);
        orderDetail.setSymbol(trade.getSymbol());
        orderDetail.setBaseUsdRate(trade.getBaseUsdRate());
        exchangeOrderDetailService.save(orderDetail);

        //修为异步保存聚合信息(聚合币币交易订单手续费明细存入mongodb)
        orderDetailAggregationService.asncySaveOrderDetailAggregation(order, trade, member, orderDetail.getFee(), feeDiscount);

        //获取收入的钱包账户
        MemberWallet incomeWalletCache = memberWalletService.findCacheByCoinUnitAndMemberId(incomeSymbol, order.getMemberId());
        if (null == incomeWalletCache) {
            //对应币种的账户不存在，则创建对应的账户（解决买币账户不存在的问题）
            Coin coin = coinService.findByUnit(incomeSymbol);
            if (null == coin) {
                throw new Exception(MessageFormat.format("交易币种不存在，币种名称={0}", incomeSymbol));
            }
            incomeWalletCache = memberWalletService.createMemberWallet(order.getMemberId(), coin);
            if (null == incomeWalletCache) {
                throw new Exception(MessageFormat.format("用户账户不存在。用户id={0},币种名称={1}", order.getMemberId().toString(), incomeSymbol));
            }
        }
        //钱包账户和会员钱包账户是否一致
        if (incomeWalletCache.getMemberId().longValue() != order.getMemberId().longValue()) {
            throw new Exception(MessageFormat.format("不是订单会员的进账钱包账户。" +
                    "钱包信息={0}，订单信息={1}", incomeWalletCache, order));
        }
        //钱包的币种和交易币种是否一致
        if (incomeWalletCache.getCoin() != null
                && !incomeWalletCache.getCoin().getUnit().equals(incomeSymbol)) {
            throw new Exception(MessageFormat.format("不是订单对应的进账币种钱包账户。" +
                    "钱包信息={0}，订单信息={1}", incomeWalletCache, order));
        }

        //获取支出的钱包账户
        MemberWallet outcomeWalletCache = memberWalletService.findCacheByCoinUnitAndMemberId(outcomeSymbol, order.getMemberId());
        //钱包账户和会员钱包账户是否一致
        if (outcomeWalletCache.getMemberId().longValue() != order.getMemberId().longValue()) {
            throw new Exception(MessageFormat.format("不是订单会员的出账钱包账户。" +
                    "钱包信息={0}，订单信息={1}", outcomeWalletCache, order));
        }
        //钱包的币种和交易币种是否一致
        if (outcomeWalletCache.getCoin() != null
                && !outcomeWalletCache.getCoin().getUnit().equals(outcomeSymbol)) {
            throw new Exception(MessageFormat.format("不是订单对应的进账币种钱包账户。" +
                    "钱包信息={0}，订单信息={1}", outcomeWalletCache, order));
        }

        //增加出资金记录
        MemberTransaction transaction2 = new MemberTransaction();
        transaction2.setAmount(outcomeCoinAmount.negate());
        transaction2.setSymbol(outcomeSymbol);
        //transaction2.setAddress("");
        transaction2.setMemberId(outcomeWalletCache.getMemberId());
        transaction2.setType(TransactionType.EXCHANGE);
        transaction2.setFee(BigDecimal.ZERO);
        transaction2.setFeeDiscount(BigDecimal.ZERO);
        transaction2.setRefId(order.getOrderId()); //设置关联的订单号
        transactionService.save(transaction2); //edit by yangch 时间： 2018.05.16 原因：设置关联的订单号

        //增加入资金记录
        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(incomeCoinAmount);
        transaction.setSymbol(incomeSymbol);
        //transaction.setAddress("");
        transaction.setMemberId(incomeWalletCache.getMemberId());
        transaction.setType(TransactionType.EXCHANGE); //币币交易
        transaction.setFee(fee.subtract(feeDiscount)); //手续费优惠
        transaction.setFeeDiscount(feeDiscount); //优惠的手续费
        transaction.setRefId(order.getOrderId()); //设置关联的订单号
        MemberTransaction transactionNew = transactionService.save(transaction); //获取保存后的对象

        //有手续费的情况下需走返佣流程
        if (transactionNew.getFee().compareTo(BigDecimal.ZERO) > 0) {
            //币币交易3级推荐返佣处理
            promoteRewardService.asycPromoteReward(transactionNew, member, incomeSymbol); //3级推荐返佣

            //区域合伙人返佣处理
            partnerRewardService.asyncPartnerReward(transactionNew, member, order, trade, exchangeCoin);
        } else {
            log.debug("交易手续费为0不走返佣流程，订单号={}", order.getOrderId());
        }

        //重要：防止同一账户同时买卖出现表死锁的情况，调整 买和卖 钱包处理的顺序
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            //增加回报的可用的币
            //incomeWallet.setBalance(incomeWallet.getBalance().add(incomeCoinAmount));
            MessageResult result1 = memberWalletService.increaseBalance(incomeWalletCache.getId(), incomeCoinAmount);
            if (result1.getCode() != 0) {
                throw new Exception(MessageFormat.format("余额增加失败！账户ID={0}，增加余额={1}"
                        , incomeWalletCache.getId().toString(), incomeCoinAmount));
            }

            //扣除付出的币
            //MemberWallet outcomeWallet = memberWalletService.findByCoinUnitAndMemberId(outcomeSymbol, order.getMemberId());
            //outcomeWallet.setFrozenBalance(outcomeWallet.getFrozenBalance().subtract(outcomeCoinAmount));
            MessageResult result2 = memberWalletService.decreaseFrozen(outcomeWalletCache.getId(), outcomeCoinAmount);
            if (result2.getCode() != 0) {
                throw new Exception(MessageFormat.format("解冻冻结余额失败！账户ID={0}，解冻数量={1}"
                        , outcomeWalletCache.getId().toString(), outcomeCoinAmount));
            }
        } else {
            //扣除付出的币
            MessageResult result2 = memberWalletService.decreaseFrozen(outcomeWalletCache.getId(), outcomeCoinAmount);
            if (result2.getCode() != 0) {
                throw new Exception(MessageFormat.format("解冻冻结余额失败！账户ID={0}，解冻数量={1}"
                        , outcomeWalletCache.getId().toString(), outcomeCoinAmount));
            }

            //增加回报的可用的币
            MessageResult result1 = memberWalletService.increaseBalance(incomeWalletCache.getId(), incomeCoinAmount);
            if (result1.getCode() != 0) {
                throw new Exception(MessageFormat.format("余额增加失败！账户ID={0}，增加余额={1}"
                        , incomeWalletCache.getId().toString(), incomeCoinAmount));
            }
        }
    }

    public List<ExchangeOrderDetail> getAggregation(String orderId) {
        return exchangeOrderDetailRepository.findAllByOrderId(orderId);
    }


    /**
     * 查询所有未完成的挂单
     *
     * @param symbol 交易对符号
     * @return
     */
    public List<ExchangeOrder> findAllTradingOrderBySymbol(String symbol) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        return exchangeOrderRepository.findAll(specification);
    }

    public List<ExchangeOrder> findAll() {
        return exchangeOrderRepository.findAll();
    }

    //add by yangch 时间： 2018.06.23 原因：缓存订单信息，更新订单时再删除缓存
    //@Cacheable(cacheNames = "exchangeOrder", key = "'entity:exchangeOrder:'+#id")
    public ExchangeOrder findOne(String id) {
        return exchangeOrderRepository.findOne(id);
    }

    /***
      * 从只读库中查询订单
      * @author yangch
      * @time 2018.07.17 14:45 
     * @param orderId 订单ID
     */
    @ReadDataSource
    public ExchangeOrder findOneReadOnly(String orderId) {
        return exchangeOrderMapper.queryByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public PageResult<ExchangeOrder> queryWhereOrPage(List<Predicate> predicates, Integer pageNo, Integer pageSize) {
        List<ExchangeOrder> list;
        JPAQuery<ExchangeOrder> jpaQuery = queryFactory.selectFrom(QExchangeOrder.exchangeOrder);
        if (predicates != null)
            jpaQuery.where(predicates.toArray(new BooleanExpression[predicates.size()]));
        jpaQuery.orderBy(QExchangeOrder.exchangeOrder.time.desc());
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        PageResult<ExchangeOrder> result = new PageResult<>(list, jpaQuery.fetchCount());
        result.setNumber(pageNo);
        result.setSize(pageSize);
        return result;
    }

    /**
     * 订单交易完成
     *
     * @param order
     * @param isRedo 是否重做的调用
     * @return
     */
    //@CacheEvict(cacheNames = "exchangeOrder", key = "'entity:exchangeOrder:'+#orderId")
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public MessageResult tradeCompleted(ExchangeOrder order, BooleanEnum isRedo) throws UnexpectedException {
        if (order == null) {
            return MessageResult.error(500, "invalid order(" + order + "),the order not exist");
        }
        /*ExchangeOrder order = exchangeOrderRepository.findByOrderId(orderId);
        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, "invalid order(" + orderId + "),not trading status");
        }
        order.setTradedAmount(tradedAmount);
        order.setTurnover(turnover);
        order.setStatus(ExchangeOrderStatus.COMPLETED);
        order.setCompletedTime(Calendar.getInstance().getTimeInMillis());
        exchangeOrderRepository.saveAndFlush(order);*/

        //更改订单状态、成交量、成交额、成交时间
        MessageResult mr = getService().completedOrder(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
        if (mr.getCode() == 0) {
            mr = returnOrderBalance(order, isRedo); //归还剩余冻结余额
        } else {
            String errMsg = "交易订单更新，订单id=" + order.getOrderId();
            return MessageResult.error(errMsg);
        }


        //edit by yangch 时间： 2018.05.23 原因：修改交易前的计划冻结余额 大于 实际交易后的冻结余额 后，需要将差额的冻结余额进行归还
        /*MessageResult mr = returnOrderBalance(order);
        if(mr.getCode() != 0){
            //失败订单记录到重做业务记录中
            throw new UnexpectedException("冻结余额归还失败，订单号:"+orderId); //不是预期的结果，可能是冻结余额不够导致的
        }*/

        //del by yangch 时间： 2018.05.11 原因：代码合并
    /*
        //下单时候冻结的币，实际成交应扣的币
        BigDecimal frozenBalance, dealBalance;
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            if (order.getType() == ExchangeOrderType.LIMIT_PRICE) {
                frozenBalance = order.getAmount().multiply(order.getPrice());
            } else {
                frozenBalance = order.getAmount();
            }
            dealBalance = turnover;
        } else {
            frozenBalance = order.getAmount();
            dealBalance = tradedAmount;
        }
        String coinSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getBaseSymbol() : order.getCoinSymbol();
        MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(coinSymbol, order.getMemberId());

        //减少付出的冻结的币
        wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(frozenBalance));
        //可用余额中增加之前冻结的币，再扣除交易的币
        wallet.setBalance(wallet.getBalance().add(frozenBalance).subtract(dealBalance));
        //保存交易记录
        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(dealBalance.negate());
        transaction.setSymbol(coinSymbol);
        transaction.setAddress("");
        transaction.setMemberId(wallet.getMemberId());
        transaction.setType(TransactionType.EXCHANGE);
        transaction.setFee(BigDecimal.ZERO);
        transactionService.save(transaction);*/
        //return MessageResult.success("tradeCompleted success");

        //如果是业务重做则返回实时的处理结果
        if (isRedo == BooleanEnum.IS_TRUE) {
            return mr;
        } else {
            return MessageResult.success();
        }
    }

    /**
     * 归还交易订单的冻结余额
     *
     * @param order  交易订单
     * @param isRedo 是否重做的调用
     * @return
     */
    //@Transactional(rollbackFor = Exception.class)
    public MessageResult returnOrderBalance(ExchangeOrder order, BooleanEnum isRedo) {
        //归还订单的解冻余额

        //下单时候冻结的币，实际成交应扣的币，退还数量
        BigDecimal frozenBalance, dealBalance, returnAmount;
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            //yangch：买的时候为 基币，如USDT
            if (order.getType() == ExchangeOrderType.LIMIT_PRICE) {
                if (order.getFreezeAmount() == null || order.getFreezeAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    //edit by yangch 时间： 2018.05.23 原因：按下单时的规则计算冻结余额
                    ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(order.getSymbol());
                    //edit by yangch 时间： 2018.09.29 原因：精度修改，应该使用价格精度
                    //frozenBalance = order.getAmount().multiply(order.getPrice()).setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_UP); //yangch:还原下单时的冻结数量
                    frozenBalance = order.getAmount().multiply(order.getPrice()).setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_UP);
                } else {
                    frozenBalance = order.getFreezeAmount();
                }
            } else {
                if (order.getFreezeAmount() == null || order.getFreezeAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    frozenBalance = order.getAmount(); //yangch：市价交易的冻结余额为委托数量
                } else {
                    frozenBalance = order.getFreezeAmount();
                }
            }

            dealBalance = order.getTurnover(); //dealBalance = turnover;
        } else {
            //yangch：卖的时候为 当前交易币
            if (order.getFreezeAmount() == null || order.getFreezeAmount().compareTo(BigDecimal.ZERO) <= 0) {
                frozenBalance = order.getAmount();
            } else {
                frozenBalance = order.getFreezeAmount();
            }

            dealBalance = order.getTradedAmount(); //dealBalance = tradedAmount; //yangch:卖出时候，成交量 即为对应的成交额
        }

        //退还金额
        returnAmount = frozenBalance.subtract(dealBalance);
        if (returnAmount.compareTo(BigDecimal.ZERO) == 0) {
            //没有要归还的冻结余额
            return MessageResult.success();
        }
        /*if( frozenBalance.subtract(dealBalance).compareTo(BigDecimal.ZERO) ==0){
            //没有要归还的冻结余额
            return mr;
        }*/

        String coinSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getBaseSymbol() : order.getCoinSymbol();
        MemberWallet walletCache = memberWalletService.findCacheByCoinUnitAndMemberId(coinSymbol, order.getMemberId());

        /*
        注：下单时没有添加交易记录，退回冻结金额时也就不添加交易记录
        //退还冻结金额 的交易记录
        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(returnAmount);
        transaction.setSymbol(coinSymbol);
        //transaction.setAddress("");
        transaction.setMemberId(walletCache.getMemberId());
        transaction.setType(TransactionType.EXCHANGE);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setFeeDiscount(BigDecimal.ZERO);
        transaction.setRefId(order.getOrderId());
        transaction.setComment("退还冻结余额"); //退还多冻结部分的余额
        transaction.setCreateTime(new Date());
        transactionService.save(transaction);*/

        //减少付出的冻结的币
        //edit by yangch 时间： 2018.05.11 原因：代码合并
        //mr = memberWalletService.thawBalance(walletCache, frozenBalance.subtract(dealBalance));
        //mr = memberWalletService.thawBalance(walletCache, returnAmount);

        MessageResult mr = getService().thawOrderBalance(walletCache, returnAmount);
        if (mr.getCode() != 0 && isRedo == BooleanEnum.IS_FALSE) {
            //记录订单中余额归还失败的订单
            String errMsg = "订单冻结余额归还失败，订单id=" + order.getOrderId() + ",账户=" + walletCache.getId() + "，币种=" + coinSymbol + "，归还数量=" + returnAmount;
            try {
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__ORDER_RETURN_BALANCE_FAIL, order.toString(), errMsg);

                return MessageResult.success();
            } catch (Exception e) {
                log.error(errMsg);
            }
            //throw new UnexpectedException("it is not the expected result, it may be that the frozen balance is not enough.the order id is:"+ order.getOrderId()); //不是预期的结果，可能是冻结余额不够导致的
        }

        return mr;
    }

    /***
      * 归还撤销订单的冻结余额
      * @author yangch
      * @time 2018.10.22 12:01 
     * @param order 交易订单
     */
    public void returnCancelOrderBalance(ExchangeOrder order) {
        try {
            getService().returnOrderBalance(order, BooleanEnum.IS_FALSE);
        } catch (Exception ex) {
            //记录订单中余额归还失败的订单
            String errMsg = "订单冻结余额归还失败，订单id=" + order.getOrderId();
            try {
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__ORDER_RETURN_BALANCE_FAIL, order.toString(), errMsg);
            } catch (Exception e) {
                log.error(errMsg);
            }
        }
    }

    /**
     * 解冻订单冻结余额
     *
     * @param wallet 钱包
     * @param amount 解冻数量
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult thawOrderBalance(MemberWallet wallet, BigDecimal amount) {
        return memberWalletService.thawBalance(wallet, amount);
    }


    /**
     * 撤销订单（正常的撤销订单，需先从内存交易队列中撤单成功）
     *
     * @param order 订单
     * @return
     */
    public MessageResult cancelOrder(ExchangeOrder order) {
        BigDecimal tradedAmount = order.getTradedAmount();
        BigDecimal turnover = order.getTurnover();

        //取消订单，并更新成交量和成交额（该方法具备幂等性）
        MessageResult mr1 = getService().cancelOrder(order.getOrderId(), tradedAmount, turnover);
        if (mr1.getCode() == 0) {
            //处理退还的金额（为了提升性能，未采用事务处理，采用补偿的方式来做）
            //edit by yangch 时间： 2018.10.22 原因：退款异常的时候，订单状态为已完成，但冻结余额可能没有归还成功，并没有告警记录的情况
            //returnOrderBalance(order, BooleanEnum.IS_FALSE);
            returnCancelOrderBalance(order);
        } else {
            //更新失败原因，可能是订单不存在、订单状态不为“交易中”
            String errMsg = "撤销订单失败，订单id=" + order.getOrderId();

            /*//记录撤单失败的订单
            try {
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__ORDER_CACLE_UPDATE_STATUS_FAIL, order.toString(), errMsg);
            } catch (Exception e) {
                log.error(errMsg);
            }*/
            return MessageResult.error(errMsg);
            //throw new UnexpectedException(errMsg);
        }

        return MessageResult.success();
    }


    /**
     * 撤销订单（不在内存交易队列中的撤单）
     *
     * @param order 订单
     * @return
     */
    //@CacheEvict(cacheNames = "exchangeOrder", key = "'entity:exchangeOrder:'+#orderId")
    //@Transactional(rollbackFor = Exception.class)
    public MessageResult cancelOrderNotInExchange(ExchangeOrder order) {
        /*
        //不需要，cancelOrder(orderId, tradedAmount, turnover) 具备幂等性
        ExchangeOrder orderNew = getService().findOne(order.getOrderId());
        if (orderNew == null) {
            return MessageResult.error("order not exists");
        }
        if (orderNew.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, "order not in trading");
        }*/

        BigDecimal tradedAmount = order.getTradedAmount();
        BigDecimal turnover = order.getTurnover();

        //处理订单不在撮单系统的内存中时，以mongodb中聚合的数据为准（需要判断订单明细的处理是否有异常）
        ///备注：本次（2018-08-10）更新后基本上不会出现内存中撤单成功，订单状态更新失败的情况

        //edit by yangch 时间： 2018.05.22 原因：兼容性处理，交易数量和交易额为0时，从mongodb中重新汇聚撮单结果
        if (tradedAmount.compareTo(BigDecimal.ZERO) <= 0 || turnover.compareTo(BigDecimal.ZERO) <= 0) {
            ExchangeOrderAggregation exchangeOrderAggregation = getService().getTradedAmountByOrderId(order);
            if (tradedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                tradedAmount = exchangeOrderAggregation.getAmount();
            }
            if (turnover.compareTo(BigDecimal.ZERO) <= 0) {
                turnover = exchangeOrderAggregation.getTurnover();
            }

            //更新成交量和成交额
            order.setTradedAmount(tradedAmount);
            order.setTurnover(turnover);
            //order.setStatus(ExchangeOrderStatus.CANCELED);
        }

        //取消订单，并更新成交量和成交额（该方法具备幂等性）
        MessageResult mr1 = getService().cancelOrder(order.getOrderId(), tradedAmount, turnover);
        if (mr1.getCode() == 0) {
            //处理退还的金额（为了提升性能，未采用事务处理，采用补偿的方式来做）
            //edit by yangch 时间： 2018.10.22 原因：退款异常的时候，订单状态为已完成，但冻结余额可能没有归还成功，并没有告警记录的情况
            //returnOrderBalance(order, BooleanEnum.IS_FALSE);
            returnCancelOrderBalance(order);
        } else {
            //更新失败原因，可能是订单不存在、订单状态不为“交易中”
            String errMsg = "撤销订单失败，订单id=" + order.getOrderId();
            return MessageResult.error(errMsg);
            //throw new UnexpectedException(errMsg);
        }

        return MessageResult.success();
    }

    /**
     * 撤销订单，同时更新成交数量和成交额
     *
     * @param orderId      订单号
     * @param tradedAmount 总交易数量
     * @param turnover     总成交额
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelOrder(String orderId, BigDecimal tradedAmount, BigDecimal turnover) {
        if (exchangeOrderRepository.cancelOrder(orderId, tradedAmount, turnover,
                Calendar.getInstance().getTimeInMillis()) > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("orderCancleFailed");
        }
    }

    /**
     * 更新为完成订单，同时更新成交数量和成交额
     *
     * @param orderId      订单号
     * @param tradedAmount 总交易数量
     * @param turnover     总成交额
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult completedOrder(String orderId, BigDecimal tradedAmount, BigDecimal turnover) {
        if (exchangeOrderRepository.completedOrder(orderId, tradedAmount, turnover,
                Calendar.getInstance().getTimeInMillis()) > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("orderCompletedFailed");
        }
    }


    /**
     * 获取指定订单号的交易数量
     * add by yangch
     *
     * @param exchangeOrder 币币交易订单
     * @return
     */
    public ExchangeOrderAggregation getTradedAmountByOrderId(ExchangeOrder exchangeOrder) {
        ExchangeOrderAggregation exchangeOrderAggregation = new ExchangeOrderAggregation();
        exchangeOrderAggregation.setOrderId(exchangeOrder.getOrderId());

        //获取交易成交详情
        BigDecimal tradedAmount = BigDecimal.ZERO; //交易数量
        BigDecimal tradedTurnover = BigDecimal.ZERO; //交易额
        List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
        if (null == details) {
            exchangeOrderAggregation.setAmount(tradedAmount);
            exchangeOrderAggregation.setTurnover(tradedTurnover);

            return exchangeOrderAggregation;
        }
        for (ExchangeOrderDetail trade : details) {
            //设置交易数量
            if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE && exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
                if (trade.getTurnover() == null || trade.getTurnover().compareTo(BigDecimal.ZERO) <= 0) {
                    //理论上很难进入该代码块，主要是兼容版本。通过 数量*价格 换算为USDT
                    ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(exchangeOrder.getSymbol());
                    //edit by yangch 时间： 2018.09.29 原因：精度修改，应该使用价格精度
                    tradedAmount = tradedAmount.add(trade.getAmount().multiply(trade.getPrice())).setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_UP);
                } else {
                    tradedAmount = tradedAmount.add(trade.getTurnover());
                }
            } else {
                tradedAmount = tradedAmount.add(trade.getAmount());
            }

            //设置交易额
            if (trade.getTurnover() == null || trade.getTurnover().compareTo(BigDecimal.ZERO) <= 0) {
                //理论上很难进入该代码块，主要是兼容版本。通过 数量*价格 换算为USDT
                ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(exchangeOrder.getSymbol());
                //edit by yangch 时间： 2018.09.29 原因：精度修改，应该使用价格精度
                //tradedTurnover = tradedTurnover.add(trade.getAmount().multiply(trade.getPrice())).setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_UP);
                tradedTurnover = tradedTurnover.add(trade.getAmount().multiply(trade.getPrice())).setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_UP);
            } else {
                tradedTurnover = tradedTurnover.add(trade.getTurnover());
            }
        }

        exchangeOrderAggregation.setAmount(tradedAmount);
        exchangeOrderAggregation.setTurnover(tradedTurnover);

        return exchangeOrderAggregation;
    }


    /**
     * 获取某交易对当日已取消次数
     *
     * @param uid
     * @param symbol
     * @return
     */
    public long findTodayOrderCancelTimes(Long uid, String symbol) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTick = calendar.getTimeInMillis(); //凌晨
        calendar.add(Calendar.HOUR_OF_DAY, 24); //第二天凌晨
        long endTick = calendar.getTimeInMillis();

        //修改为从只读库查询
        /*Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("memberId",uid,false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.CANCELED, false));
        specification.add(Restrictions.gte("canceledTime",startTick,false));
        specification.add(Restrictions.lt("canceledTime",endTick,false));
        return exchangeOrderRepository.count(specification);*/
        return exchangeOrderMapper.findTodayOrderCancelTimes(symbol, uid, startTick, endTick);
    }

    /**
     * 查询当前正在交易的订单数量
     *
     * @param uid
     * @param symbol
     * @return
     */
    public long findCurrentTradingCount(Long uid, String symbol) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        return exchangeOrderRepository.count(specification);
    }

    public long findCurrentTradingCount(Long uid, String symbol, ExchangeOrderDirection direction) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("direction", direction, false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        return exchangeOrderRepository.count(specification);
    }

    public List<ExchangeOrder> findOvertimeOrder(String symbol, int maxTradingTime) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        specification.add(Restrictions.eq("symbol", symbol, false));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -maxTradingTime);
        long tickTime = calendar.getTimeInMillis();
        specification.add(Restrictions.lt("time", tickTime, false));
        return exchangeOrderRepository.findAll(specification);
    }

    /***
      * 查询超时订单
      * @author yangch
      * @time 2018.07.26 15:38 
     * @param symbol 交易端
     * @param maxTradingTime 最大委托时间（单位：秒）
     */
    public List<ExchangeOrder> findOvertimeOrderRreadOnly(String symbol, int maxTradingTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -maxTradingTime);
        long tickTime = calendar.getTimeInMillis(); //超时时间错截点
        return exchangeOrderMapper.findOvertimeOrder(symbol, tickTime);
    }

    public ExchangeOrderService getService() {
        return SpringContextUtil.getBean(ExchangeOrderService.class);
    }
}
