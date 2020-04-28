package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderStatus;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dao.MemberTransactionDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.exception.MessageCodeException;
import com.spark.bitrade.mapper.dao.ExchangeFastOrderMapper;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.AssertUtil;
import com.spark.bitrade.util.IdWorkByTwitter;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@Slf4j
public class ExchangeFastOrderServiceImpl
        extends ServiceImpl<ExchangeFastOrderMapper, ExchangeFastOrderDO>
        implements IExchangeFastOrderService {

    @Autowired
    private IExchangeFastCoinService fastCoinService;

    @Autowired
    private IExchangeFastAccountService fastAccountService;


    @Autowired
    private MemberWalletService walletService;

    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private MemberTransactionDao transactionDao;

    @Autowired(required = false)
    @Qualifier("idWorkByTwitterSnowflake")
    private IdWorkByTwitter idWorkByTwitterSnowflake;


    @Override
    public ExchangeFastOrderDO exchangeInitiator(Long memberId, String appId,
                                                 String coinSymbol, String baseSymbol,
                                                 BigDecimal amount, ExchangeOrderDirection direction,
                                                 BigDecimal currentPrice) {
        // 1、校验输入参数
        AssertUtil.notNull(memberId, MessageCode.INVALID_PARAMETER);
        AssertUtil.notNull(coinSymbol, MessageCode.INVALID_PARAMETER);
        AssertUtil.notNull(baseSymbol, MessageCode.INVALID_PARAMETER);
        AssertUtil.notNull(amount, MessageCode.INVALID_PARAMETER);
        AssertUtil.notNull(direction, MessageCode.INVALID_PARAMETER);
        AssertUtil.notNull(currentPrice, MessageCode.INVALID_PARAMETER);
        AssertUtil.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, MessageCode.BAD_PARAMETER_FORMAT);

        // 2、获取闪兑汇率，并验证汇率不能为0
        AssertUtil.isTrue(currentPrice.compareTo(BigDecimal.ZERO) > 0, MessageCode.INVALID_EXCHANGE_RATE);

        // 3、验证币种是否支持闪兑
        ExchangeFastCoinDO exchangeFastCoin = fastCoinService.findByAppIdAndCoinSymbol(appId, coinSymbol, baseSymbol);
        AssertUtil.notNull(exchangeFastCoin, MessageCode.NONSUPPORT_FAST_EXCHANGE_COIN);
        AssertUtil.notNull(fastCoinService.list4BaseSymbol(appId).contains(baseSymbol),
                MessageCode.NONSUPPORT_FAST_EXCHANGE_COIN);

        // 4、验证闪兑用户是否存在
        ExchangeFastAccountDO fastAccount = fastAccountService.findByAppIdAndCoinSymbol(appId, coinSymbol, baseSymbol);
        AssertUtil.notNull(fastAccount, MessageCode.MISSING_FAST_EXCHANGE_ACCOUNT);

        // 5、构建闪兑订单
        ExchangeFastOrderDO order = this.buildExchangeFastOrder(memberId, appId, coinSymbol, baseSymbol,
                amount, direction, currentPrice, fastAccount, exchangeFastCoin);

        // 6、处理 收入、支出 的币种和数量
        // 收入、支出 币种
        String incomeSymbol, outcomeSymbol;
        // 收入、支出 数量
        BigDecimal incomeCoinAmount, outcomeCoinAmount;
        if (direction == ExchangeOrderDirection.BUY) {
            //买入场景： 闪兑基币币种 -> 闪兑币种
            incomeSymbol = coinSymbol;
            outcomeSymbol = baseSymbol;
            incomeCoinAmount = order.getTradedAmount();
            outcomeCoinAmount = order.getAmount();
        } else {
            //卖出场景：闪兑币种 -> 闪兑基币币种
            incomeSymbol = baseSymbol;
            outcomeSymbol = coinSymbol;
            incomeCoinAmount = order.getTradedAmount();
            outcomeCoinAmount = order.getAmount();
        }

        // 7、获取钱包信息
        // 7.1 获取收入的钱包账户
        MemberWallet incomeWalletCache = walletService.findCacheByCoinUnitAndMemberId(incomeSymbol, order.getMemberId());
        AssertUtil.notNull(incomeWalletCache, MessageCode.MISSING_ACCOUNT);

        // 7.2 获取支出的钱包账户
        MemberWallet outcomeWalletCache = walletService.findCacheByCoinUnitAndMemberId(outcomeSymbol, order.getMemberId());
        AssertUtil.notNull(outcomeWalletCache, MessageCode.MISSING_ACCOUNT);

        // 8、构建资金记录
        // 8.1 构建资金支出记录
        MemberTransaction transaction2 = this.buildOutcomeTransaction(order.getMemberId(), order.getOrderId().toString(), outcomeSymbol, outcomeCoinAmount);
        // 8.2 构建资金收入记录
        MemberTransaction transaction = this.buildIncomeTransaction(order.getMemberId(), order.getOrderId().toString(), incomeSymbol, incomeCoinAmount);

        // 9、通过事务 保存 订单、账户支出、账户收入、支出和收入记录
        getService().doWithTransactional(order,
                incomeCoinAmount, outcomeCoinAmount,
                incomeWalletCache, outcomeWalletCache,
                transaction2, transaction);

        // 10、异步调用或异步通知 兑换处理方处理总账户逻辑
        getService().asyncExchangeReceiver(order.getOrderId());

        return order;
    }


    /**
     * @param order
     * @param incomeCoinAmount
     * @param outcomeCoinAmount
     * @param incomeWalletCache
     * @param outcomeWalletCache
     * @param transaction2
     * @param transaction
     */
    @Transactional(rollbackFor = {Exception.class, MessageCodeException.class})
    public void doWithTransactional(ExchangeFastOrderDO order, BigDecimal incomeCoinAmount,
                                    BigDecimal outcomeCoinAmount, MemberWallet incomeWalletCache,
                                    MemberWallet outcomeWalletCache,
                                    MemberTransaction transaction2, MemberTransaction transaction) {
        //保存兑换订单
        this.baseMapper.insert(order);

        //增加出资金记录
        transactionService.save(transaction2);
        ///transactionDao.saveAndFlush(transaction2);

        //增加入资金记录
        transactionService.save(transaction);
        ///transactionDao.saveAndFlush(transaction);

        //扣除闪兑数量、添加闪兑获得币的数据
        getService().updateWallet(order, incomeCoinAmount, outcomeCoinAmount, incomeWalletCache, outcomeWalletCache);
    }


    @Transactional(rollbackFor = {Exception.class, MessageCodeException.class})
    public void updateWallet(ExchangeFastOrderDO order, BigDecimal incomeCoinAmount,
                             BigDecimal outcomeCoinAmount, MemberWallet incomeWalletCache, MemberWallet outcomeWalletCache)
            throws MessageCodeException {
        //重要：防止同一账户同时买卖出现表死锁的情况，调整 买和卖 钱包处理的顺序
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            //增加回报的可用的币
            MessageResult result1 = walletService.increaseBalance(incomeWalletCache.getId(), incomeCoinAmount);
            if (result1.getCode() != 0) {
                log.warn("余额增加失败！账户ID={}，增加余额={}", incomeWalletCache.getId(), incomeCoinAmount);
                throw new MessageCodeException(MessageCode.FAILED_ADD_BALANCE);
            }

            //扣除付出的币
            MessageResult result2 = walletService.decreaseBalance(outcomeWalletCache.getId(), outcomeCoinAmount);
            if (result2.getCode() != 0) {
                log.warn("解冻冻结余额失败！账户ID={}，解冻数量={}", outcomeWalletCache.getId(), outcomeCoinAmount);
                throw new MessageCodeException(MessageCode.FAILED_SUBTRACT_BALANCE);
            }
        } else {
            //扣除付出的币
            MessageResult result2 = walletService.decreaseBalance(outcomeWalletCache.getId(), outcomeCoinAmount);
            if (result2.getCode() != 0) {
                log.warn("解冻冻结余额失败！账户ID={}，解冻数量={}", outcomeWalletCache.getId(), outcomeCoinAmount);
                throw new MessageCodeException(MessageCode.FAILED_SUBTRACT_BALANCE);
            }

            //增加回报的可用的币
            MessageResult result1 = walletService.increaseBalance(incomeWalletCache.getId(), incomeCoinAmount);
            if (result1.getCode() != 0) {
                log.warn("余额增加失败！账户ID={}，增加余额={}", incomeWalletCache.getId(), incomeCoinAmount);
                throw new MessageCodeException(MessageCode.FAILED_ADD_BALANCE);
            }
        }
    }

    /**
     * 构建收入记录
     *
     * @param memberId         快速转账订单
     * @param refId            关联订单号
     * @param incomeSymbol     收入币种
     * @param incomeCoinAmount 收入币数量
     * @return
     */
    private MemberTransaction buildIncomeTransaction(Long memberId, String refId, String incomeSymbol, BigDecimal incomeCoinAmount) {
        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(incomeCoinAmount);
        transaction.setSymbol(incomeSymbol);
        transaction.setAddress(null);
        transaction.setMemberId(memberId);
        //闪兑
        transaction.setType(TransactionType.EXCHANGE_FAST);
        //手续费优惠
        transaction.setFee(BigDecimal.ZERO);
        //优惠的手续费
        transaction.setFeeDiscount(BigDecimal.ZERO);
        //设置关联的订单号
        transaction.setRefId(refId);
        transaction.setFeeDiscountAmount(BigDecimal.ZERO);
        transaction.setFeeDiscountCoinUnit(null);
        return transaction;
    }

    /**
     * 构建支出记录
     *
     * @param memberId          快速转账订单
     * @param refId             关联订单号
     * @param outcomeSymbol     支出币种
     * @param outcomeCoinAmount 支出币数量
     * @return
     */
    private MemberTransaction buildOutcomeTransaction(Long memberId, String refId, String outcomeSymbol, BigDecimal outcomeCoinAmount) {
        return buildIncomeTransaction(memberId, refId, outcomeSymbol, outcomeCoinAmount.negate());
        /*MemberTransaction transaction2 = new MemberTransaction();
        transaction2.setAmount(outcomeCoinAmount.negate());
        transaction2.setSymbol(outcomeSymbol);
        transaction2.setAddress(null);
        transaction2.setMemberId(memberId);
        transaction2.setType(TransactionType.EXCHANGE_FAST);
        transaction2.setFee(BigDecimal.ZERO);
        transaction2.setFeeDiscount(BigDecimal.ZERO);
        //设置关联的订单号
        transaction2.setRefId(refId);
        transaction2.setFeeDiscountAmount(BigDecimal.ZERO);
        transaction2.setFeeDiscountCoinUnit(null);
        return transaction2;*/
    }

    /**
     * 构建闪兑订单记录
     *
     * @param memberId     会员ID
     * @param coinSymbol   兑换币种
     * @param baseSymbol   兑换基币
     * @param amount       兑换数量
     * @param direction    兑换方向
     * @param currentPrice 汇率，汇率和兑换方向有关
     * @param fastAccount  闪兑总账户
     * @return 生成闪兑订单信息
     */
    private ExchangeFastOrderDO buildExchangeFastOrder(Long memberId, String appId, String coinSymbol,
                                                       String baseSymbol, BigDecimal amount,
                                                       ExchangeOrderDirection direction, BigDecimal currentPrice,
                                                       ExchangeFastAccountDO fastAccount, ExchangeFastCoinDO exchangeFastCoin) {
        ExchangeFastOrderDO order = new ExchangeFastOrderDO();
        order.setOrderId(idWorkByTwitterSnowflake != null ? idWorkByTwitterSnowflake.nextId() : IdWorker.getId());
        order.setMemberId(memberId);
        order.setBaseSymbol(baseSymbol);
        order.setCoinSymbol(coinSymbol);
        order.setAmount(amount);
        order.setDirection(direction);
        order.setCurrentPrice(currentPrice);
        order.setInitiatorStatus(ExchangeOrderStatus.COMPLETED);
        order.setReceiverStatus(ExchangeOrderStatus.TRADING);
        order.setCreateTime(System.currentTimeMillis());
        order.setCompletedTime(System.currentTimeMillis());
        order.setReceiveId(fastAccount.getMemberId());
        order.setAppId(appId);

        BigDecimal adjustRate, tradedPrice, tradedAmount, virtualBrokerageFee;
        if (direction == ExchangeOrderDirection.BUY) {
            //买入场景： 兑换基币币种 -> 接收币种
            //输入：
            //   基币(BT)=1 -> 兑换币(BTC)=4， 汇率计算 = currentPrice= BT/BTC=1/4=0.25
            //   兑换数量(BT)：amount = 100 (用户输入)
            //   调整的比例：adjustRate = 0.05
            //计算：
            //  tradedPrice(成交价格) = currentPrice[0.25]*(1-adjustRate[0.05]) = 0.2375
            //  tradedAmount(成交数量BTC) = amount[100] * tradedPrice[0.2375] = 23.75 <25(正常)
            //  virtualBrokerageFee(虚拟佣金BTC) = amount[100] * currentPrice[0.25] - tradedAmount[23.75] = 1.25

            //如未配置浮动比例则获取默认配置
            if (!StringUtils.isEmpty(fastAccount.getBuyAdjustRate())) {
                adjustRate = fastAccount.getBuyAdjustRate();
            } else {
                adjustRate = StringUtils.isEmpty(exchangeFastCoin.getBuyAdjustRate())
                        ? BigDecimal.ZERO
                        : exchangeFastCoin.getBuyAdjustRate();
            }
        } else {
            //卖出场景：支付币种 ->兑换基币币种
            //输入：
            //   兑换币(BTC)=4 -> 基币(BT)=1， 汇率计算 = currentPrice= BTC/BT=4/1=4
            //   兑换数量(BTC)：amount = 25 (用户输入)
            //   调整的比例：adjustRate = 0.05
            //计算：
            //  tradedPrice(成交价格) = currentPrice[4]*(1-adjustRate[0.05]) = 3.8
            //  tradedAmount(成交数量BT) = amount[25] * tradedPrice[3.8] = 95 < 100(正常)
            //  virtualBrokerageFee(虚拟佣金BT) = amount[25] * currentPrice[4] - tradedAmount[95] = 5

            //如未配置浮动比例则获取默认配置
            if (!StringUtils.isEmpty(fastAccount.getSellAdjustRate())) {
                adjustRate = fastAccount.getSellAdjustRate();
            } else {
                adjustRate = StringUtils.isEmpty(exchangeFastCoin.getSellAdjustRate())
                        ? BigDecimal.ZERO
                        : exchangeFastCoin.getSellAdjustRate();
            }
        }

        //成交价，根据实时汇率、闪兑浮动比例以及方向计算出来的成交价
        tradedPrice = currentPrice.multiply(BigDecimal.ONE.subtract(adjustRate)).setScale(8, BigDecimal.ROUND_DOWN);
        //成交数量
        tradedAmount = amount.multiply(tradedPrice).setScale(8, BigDecimal.ROUND_DOWN);
        //虚拟佣金
        virtualBrokerageFee = amount.multiply(currentPrice).setScale(8, BigDecimal.ROUND_DOWN).subtract(tradedAmount);

        order.setAdjustRate(adjustRate);
        order.setTradedPrice(tradedPrice);
        order.setTradedAmount(tradedAmount);
        order.setVirtualBrokerageFee(virtualBrokerageFee);

        return order;
    }

    @Override
    public void exchangeReceiver(Long orderId) {
        // 1、校验输入的订单是否存在
        ExchangeFastOrderDO order = getService().selectById(orderId);
        AssertUtil.notNull(order, MessageCode.NONEXISTENT_ORDER);

        // 2、验证“兑换发起方处理状态”状态是否为“完成”
        AssertUtil.isTrue(order.getInitiatorStatus() == ExchangeOrderStatus.COMPLETED, MessageCode.UNMATCHED_STATUS);

        // 3、验证“兑换接收方处理状态”状态是否为“交易中”
        if (order.getReceiverStatus() == ExchangeOrderStatus.COMPLETED) {
            return;
        }

        // 4、处理 收入、支出 的币种和数量
        // 收入、支出 币种
        String incomeSymbol, outcomeSymbol;
        // 收入、支出 数量
        BigDecimal incomeCoinAmount, outcomeCoinAmount;
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            //闪兑用户 买入场景： 闪兑基币币种 -> 闪兑币种(总账的处理与闪兑用户的账是相反的)
            incomeSymbol = order.getBaseSymbol();
            outcomeSymbol = order.getCoinSymbol();
            incomeCoinAmount = order.getAmount();
            outcomeCoinAmount = order.getTradedAmount();
        } else {
            //闪兑用户 卖出场景：闪兑币种 -> 闪兑基币币种(总账的处理与闪兑用户的账是相反的)
            incomeSymbol = order.getCoinSymbol();
            outcomeSymbol = order.getBaseSymbol();
            incomeCoinAmount = order.getAmount();
            outcomeCoinAmount = order.getTradedAmount();
        }

        // 7、获取钱包信息
        // 7.1 获取收入的钱包账户
        MemberWallet incomeWalletCache = walletService.findCacheByCoinUnitAndMemberId(incomeSymbol, order.getReceiveId());
        AssertUtil.notNull(incomeWalletCache, MessageCode.MISSING_ACCOUNT);

        // 7.2 获取支出的钱包账户
        MemberWallet outcomeWalletCache = walletService.findCacheByCoinUnitAndMemberId(outcomeSymbol, order.getReceiveId());
        AssertUtil.notNull(outcomeWalletCache, MessageCode.MISSING_ACCOUNT);

        // 8、构建资金记录
        // 8.1 构建资金支出记录
        MemberTransaction transaction2 = buildOutcomeTransaction(order.getReceiveId(), orderId.toString(), outcomeSymbol, outcomeCoinAmount);
        // 8.2 构建资金收入记录
        MemberTransaction transaction = buildIncomeTransaction(order.getReceiveId(), orderId.toString(), incomeSymbol, incomeCoinAmount);

        getService().doWithTransactional4Receiver(order, incomeCoinAmount,
                outcomeCoinAmount, incomeWalletCache,
                outcomeWalletCache,
                transaction2, transaction);
    }

    @Async
    public void asyncExchangeReceiver(Long orderId) {
        //延迟处理，防止主从不同步
        try {
            Thread.sleep(500);
        } catch (Exception ex) {
        }

        this.exchangeReceiver(orderId);
    }

    /**
     * 通过事务处理接收方
     *
     * @param order
     * @param incomeCoinAmount
     * @param outcomeCoinAmount
     * @param incomeWalletCache
     * @param outcomeWalletCache
     * @param transaction2
     * @param transaction
     */
    @Transactional(rollbackFor = {Exception.class, MessageCodeException.class})
    public void doWithTransactional4Receiver(ExchangeFastOrderDO order, BigDecimal incomeCoinAmount,
                                             BigDecimal outcomeCoinAmount, MemberWallet incomeWalletCache,
                                             MemberWallet outcomeWalletCache,
                                             MemberTransaction transaction2, MemberTransaction transaction) {
        //修改“闪兑订单.兑换接收方处理状态”为“完成”、并更新“成交时间”
        int updateCount = this.baseMapper.updataReceiverStatus(order.getOrderId(),
                ExchangeOrderStatus.TRADING, ExchangeOrderStatus.COMPLETED, System.currentTimeMillis());
        AssertUtil.isTrue(updateCount == 1, MessageCode.UNKNOW_ERROR);

        //增加出资金记录
        transactionService.save(transaction2);
        ///transactionDao.saveAndFlush(transaction2);

        //增加入资金记录
        transactionService.save(transaction);
        ///transactionDao.saveAndFlush(transaction);

        //扣除闪兑数量、添加闪兑获得币的数据
        getService().updateWallet(order, incomeCoinAmount, outcomeCoinAmount, incomeWalletCache, outcomeWalletCache);
    }


    public ExchangeFastOrderServiceImpl getService() {
        return SpringContextUtil.getBean(ExchangeFastOrderServiceImpl.class);
    }
}
