package com.spark.bitrade.service;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.UnLockCoinDetailDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.LockCoinDetailMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PriceUtil;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 解锁锁仓活动记录service
 * @author tansitao
 * @time 2018/6/30 10:39 
 */
@Service
@Slf4j
public class UnLockCoinDetailService extends BaseService {

    @Autowired
    private UnLockCoinDetailDao unLockCoinDetailDao;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private LockCoinRechargeSettingService lockCoinRechargeSettingService;
    @Autowired
    private UnlockCoinTaskService unlockCoinTaskService;

    public UnlockCoinDetail findOne(long id){
        return unLockCoinDetailDao.findOne(id);
    }

    public UnlockCoinDetail save(UnlockCoinDetail unlockCoinDetail){
        return unLockCoinDetailDao.save(unlockCoinDetail);
    }

    /**
     * 解锁到期，用户选择结算方式，解锁投资活动
     * @author tansitao
     * @time 2018/8/2 11:05 
     */
    public void UnLockFinancialLock (SettlementType settlementType, LockCoinDetail lockCoinDetail, RestTemplate restTemplate){
        try {
            //通过不同的结算方式，调用不同的结算方法
            if(settlementType == SettlementType.USDT_SETTL){
                //更新解锁状态
                if(lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKING, LockStatus.LOCKED, lockCoinDetail.getId()) > 0){
                    getService().unlockFinanCoinByUSDT(lockCoinDetail, restTemplate);
                }else {
                    log.error("===========解锁投资活动失败===========lockCoinDetail：" + lockCoinDetail.getId());
                }
            }
            else {
                if(lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKING,  LockStatus.LOCKED, lockCoinDetail.getId()) > 0){
                    getService().unlockFinanCoinByActCoin(lockCoinDetail, restTemplate);
                }else {
                    log.error("===========解锁投资活动失败===========lockCoinDetail：" + lockCoinDetail.getId());
                }
            }
        }
        catch (Exception e){
            lockCoinDetailService.updateLockStatus(LockStatus.LOCKED, LockStatus.UNLOCKING, lockCoinDetail.getId());
            log.error("===========解锁投资活动失败===========lockCoinDetail：" + lockCoinDetail.getId(),e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
      * 解锁锁仓活动记录
      * @author tansitao
      * @time 2018/7/19 15:54 
      */
    @Transactional(rollbackFor = Exception.class)
    public void unlockCoin(LockCoinDetail lockCoinDetail, RestTemplate restTemplate) throws Exception
    {
        //获取钱包信息
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),lockCoinDetail.getMemberId());
        if(memberWallet != null){
            //冻结锁仓币数
            MessageResult walletResult = memberWalletService.thawBalanceFromLockBlance(memberWallet, lockCoinDetail.getTotalAmount());
            if (walletResult.getCode() != 0){
                log.error("=======解锁锁仓活动失败lockCoinDetailId：" + lockCoinDetail.getId() + "========钱包异常");
                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
            }

            walletResult = memberWalletService.increaseBalance(memberWallet.getId(), lockCoinDetail.getPlanIncome());
            if (walletResult.getCode() != 0){
                log.error("=======解锁锁仓活动失败lockCoinDetailId：" + lockCoinDetail.getId() + "========钱包增加收益异常");
                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
            }

            //保存增加理财币种锁仓资金记录
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setAmount(lockCoinDetail.getTotalAmount().add(lockCoinDetail.getPlanIncome()));
            memberTransaction.setMemberId(lockCoinDetail.getMemberId());
//            memberTransaction.setType(TransactionType.ADMIN_LOCK_ACTIVITY);
            //add by tansitao 时间： 2018/11/7 原因：设置流水的资金记录
            if(lockCoinDetail.getType() == LockType.STO){
                memberTransaction.setType(TransactionType.STO_ACTIVITY);
            }else if(lockCoinDetail.getType() == LockType.LOCK_ACTIVITY){
                memberTransaction.setType(TransactionType.ADMIN_LOCK_ACTIVITY);
            }
            memberTransaction.setSymbol(lockCoinDetail.getCoinUnit());
            memberTransactionService.save(memberTransaction);

            //修改锁仓记录信息
            lockCoinDetail.setUnlockTime(new Date());
            lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
            lockCoinDetail.setStatus(LockStatus.UNLOCKED);
            lockCoinDetail.setCancleTime(new Date());
            lockCoinDetailService.save(lockCoinDetail);

            //获取活动币种最新USDT价格
            PriceUtil priceUtil = new PriceUtil();
            BigDecimal coinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinDetail.getCoinUnit());
            //如果价格为0，则说明价格异常
            if(coinPrice.compareTo(BigDecimal.ZERO) == 0){
                throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
            }

            //添加解锁记录
            UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
            unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
            unlockCoinDetail.setAmount(lockCoinDetail.getTotalAmount());
            unlockCoinDetail.setRemainAmount(lockCoinDetail.getRemainAmount());
            unlockCoinDetail.setPrice(coinPrice);
            save(unlockCoinDetail);

        }
        else
        {
            log.error("=======解锁锁仓活动失败lockDetailId:" + lockCoinDetail.getId() + "memberId:" + lockCoinDetail.getMemberId() + "========无钱包" + lockCoinDetail.getCoinUnit());
            throw new IllegalArgumentException("钱包不存在");
        }
    }

    /**
      * 以usdt的结算方式解锁理财锁仓活动记录
      * @author tansitao
      * @time 2018/7/31 11:44 
      */
    @Transactional(rollbackFor = Exception.class)
    public void unlockFinanCoinByUSDT(LockCoinDetail lockCoinDetail, RestTemplate restTemplate) throws Exception
    {
        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
        BigDecimal coinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinDetail.getCoinUnit());
        //如果价格为0，则说明价格异常
        if(coinPrice.compareTo(BigDecimal.ZERO) == 0){
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //获取USDT的人民币价格
        BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
        //如果价格为0，则说明价格异常
        if(usdtPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //计算预计收益金额
        BigDecimal lockEarnings = lockCoinDetail.getTotalAmount().multiply(coinPrice).multiply(usdtPrice);
        BigDecimal unlockUSDTNum;
        IncomeType incomeType;
        //比较现在价格是否大于收益价格
        if(lockEarnings.compareTo(lockCoinDetail.getTotalCNY().add(lockCoinDetail.getPlanIncome())) >= 0){
            //设置收益类型，和收益总USDT数
            incomeType = IncomeType.FINANCIAL_A1;
            unlockUSDTNum = lockCoinDetail.getTotalAmount().multiply(coinPrice);
            //获取钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),lockCoinDetail.getMemberId());
            if(memberWallet == null) {
                log.error("=======解锁投资活动失败lockDetailId:" + lockCoinDetail.getId() + "memberId:" + lockCoinDetail.getMemberId() + "========无钱包" + lockCoinDetail.getCoinUnit());
                throw new IllegalArgumentException("钱包不存在");
            }
            //获取、增加用户USDT钱包余额
            MemberWallet usdtMemberWallet = memberWalletService.findByCoinUnitAndMemberId("USDT", lockCoinDetail.getMemberId());
            if(usdtMemberWallet == null){
                throw new IllegalArgumentException("钱包不存在");
            }

            //减少锁仓币数
            MessageResult activityWalletResult = memberWalletService.decreaseLockBalance(memberWallet.getId(), lockCoinDetail.getTotalAmount());
            if (activityWalletResult.getCode() != 0){
                throw new IllegalArgumentException("可用余额不足");
            }
           //增加用户USDT数
            MessageResult usdtWalletResult = memberWalletService.increaseBalance(usdtMemberWallet.getId(), unlockUSDTNum);
            if(usdtWalletResult.getCode() != 0 ){
                throw new IllegalArgumentException("USDT添加余额失败");
            }

            //保存增加理财币种锁仓资金记录
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setAmount(unlockUSDTNum);
            memberTransaction.setMemberId(lockCoinDetail.getMemberId());
            memberTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
            memberTransaction.setSymbol("USDT");
            memberTransaction.setComment("SLB投资活动解锁（收益大于年利率）");
            memberTransactionService.save(memberTransaction);
        }
        //如果币价涨幅没有达到年化率则补充差价
        else {
            //设置收益类型，和收益总USDT数
            incomeType = IncomeType.FINANCIAL_B1;
            unlockUSDTNum = lockCoinDetail.getTotalCNY().add(lockCoinDetail.getPlanIncome()).divide(usdtPrice,8, BigDecimal.ROUND_DOWN);
            //本金数
            BigDecimal principalUSDTNum = lockCoinDetail.getTotalAmount().multiply(coinPrice);
            //平台补充的差价数
            BigDecimal differenceUSDTNum = unlockUSDTNum.subtract(principalUSDTNum);
            if(differenceUSDTNum.compareTo(BigDecimal.ZERO) >= 0){
                //获取钱包信息
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),lockCoinDetail.getMemberId());
                if(memberWallet == null) {
                    log.error("=======解锁投资活动失败lockDetailId:" + lockCoinDetail.getId() + "memberId:" + lockCoinDetail.getMemberId() + "========无钱包" + lockCoinDetail.getCoinUnit());
                    throw new IllegalArgumentException("钱包不存在");
                }
                //获取、增加用户USDT钱包余额
                MemberWallet usdtMemberWallet = memberWalletService.findByCoinUnitAndMemberId("USDT", lockCoinDetail.getMemberId());
                if(usdtMemberWallet == null){
                    throw new IllegalArgumentException("钱包不存在");
                }

                //保存增加理财币种锁仓 本金资金记录
                MemberTransaction principalTransaction = new MemberTransaction();
                principalTransaction.setAmount(principalUSDTNum);
                principalTransaction.setMemberId(lockCoinDetail.getMemberId());
                principalTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
                principalTransaction.setSymbol("USDT");
                principalTransaction.setComment("SLB投资活动解锁（收益小于年利率--返还本金）");
                memberTransactionService.save(principalTransaction);

                //保存增加理财币种锁仓 本金资金记录
                MemberTransaction differenceTransaction = new MemberTransaction();
                differenceTransaction.setAmount(differenceUSDTNum);
                differenceTransaction.setMemberId(lockCoinDetail.getMemberId());
                differenceTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
                differenceTransaction.setSymbol("USDT");
                differenceTransaction.setComment("SLB投资活动解锁（收益小于年利率--返还差价）");
                memberTransactionService.save(differenceTransaction);

                //减少锁仓币数
                MessageResult activityWalletResult = memberWalletService.decreaseLockBalance(memberWallet.getId(), lockCoinDetail.getTotalAmount());
                if (activityWalletResult.getCode() != 0){
                    throw new IllegalArgumentException("可用余额不足");
                }
                MessageResult usdtWalletResult = memberWalletService.increaseBalance(usdtMemberWallet.getId(), unlockUSDTNum);
                if(usdtWalletResult.getCode() != 0 ){
                    throw new IllegalArgumentException("USDT添加余额失败");
                }

            }
            else {
                throw new IllegalArgumentException("==本金不小于实际返回佣金==");
            }
        }

        //修改锁仓记录信息
        lockCoinDetail.setUnlockTime(new Date());
        lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
        lockCoinDetail.setStatus(LockStatus.UNLOCKED);
        lockCoinDetail.setCancleTime(new Date());
        lockCoinDetailService.save(lockCoinDetail);

        //添加解锁记录
        UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
        unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
        unlockCoinDetail.setAmount(lockCoinDetail.getTotalAmount());
        unlockCoinDetail.setRemainAmount(lockCoinDetail.getRemainAmount());
        unlockCoinDetail.setPrice(coinPrice);
        unlockCoinDetail.setSettlementType(SettlementType.USDT_SETTL);
        unlockCoinDetail.setUsdtPriceCNY(usdtPrice);
        unlockCoinDetail.setSettlementAmount(unlockUSDTNum);
        unlockCoinDetail.setIncomeType(incomeType);
        save(unlockCoinDetail);

    }

    /**
      * 以活动币种解锁理财锁仓活动记录
      * @author tansitao
      * @time 2018/7/31 11:44 
      */
    @Transactional(rollbackFor = Exception.class)
    public void unlockFinanCoinByActCoin(LockCoinDetail lockCoinDetail, RestTemplate restTemplate) throws Exception
    {
        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
        BigDecimal coinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinDetail.getCoinUnit());
        //如果价格为0，则说明价格异常
        if(coinPrice.compareTo(BigDecimal.ZERO) == 0){
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //获取USDT的人民币价格
        BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
        //如果价格为0，则说明价格异常
        if(usdtPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //计算预计收益金额
        BigDecimal lockEarnings = lockCoinDetail.getTotalAmount().multiply(coinPrice).multiply(usdtPrice);
        BigDecimal unlockCoinNum;
        IncomeType incomeType;
        //比较现在价格是否大于收益价格
        if(lockEarnings.compareTo(lockCoinDetail.getTotalCNY().add(lockCoinDetail.getPlanIncome())) >= 0){
            //设置收益类型，和收益总活动解锁币数
            incomeType = IncomeType.FINANCIAL_A1;
            unlockCoinNum = lockCoinDetail.getTotalAmount();
            //获取钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(), lockCoinDetail.getMemberId());
            if(memberWallet == null) {
                log.error("=======解锁投资活动失败lockDetailId:" + lockCoinDetail.getId() + "memberId:" + lockCoinDetail.getMemberId() + "========无钱包" + lockCoinDetail.getCoinUnit());
                throw new IllegalArgumentException("钱包不存在");
            }

            //保存增加理财币种锁仓资金记录
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setAmount(unlockCoinNum);
            memberTransaction.setMemberId(lockCoinDetail.getMemberId());
            memberTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
            memberTransaction.setSymbol(lockCoinDetail.getCoinUnit());
            memberTransaction.setComment("SLB投资活动解锁（收益大于年利率）");
            memberTransactionService.save(memberTransaction);
            //解冻锁仓余额
            MessageResult walletResult = memberWalletService.thawBalanceFromLockBlance(memberWallet, lockCoinDetail.getTotalAmount());
            if (walletResult.getCode() != 0){
                throw new IllegalArgumentException("可用余额不足");
            }
        }
        //如果币价涨幅没有达到年化率则补充差价
        else {
            //设置收益类型，和收益总活动币数
            incomeType = IncomeType.FINANCIAL_B1;
            unlockCoinNum = lockCoinDetail.getTotalCNY().add(lockCoinDetail.getPlanIncome()).divide(coinPrice.multiply(usdtPrice),8, BigDecimal.ROUND_DOWN);
            //本金数
            BigDecimal principalCoinNum = lockCoinDetail.getTotalAmount();
            //平台补充的差价数
            BigDecimal differenceCoinNum = unlockCoinNum.subtract(principalCoinNum);
            if(differenceCoinNum.compareTo(BigDecimal.ZERO) >= 0){
                //获取钱包信息
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),lockCoinDetail.getMemberId());
                if(memberWallet == null) {
                    log.error("=======解锁投资活动失败lockDetailId:" + lockCoinDetail.getId() + "memberId:" + lockCoinDetail.getMemberId() + "========无钱包" + lockCoinDetail.getCoinUnit());
                    throw new IllegalArgumentException("钱包不存在");
                }
                //保存增加理财币种锁仓 本金资金记录
                MemberTransaction principalTransaction = new MemberTransaction();
                principalTransaction.setAmount(principalCoinNum);
                principalTransaction.setMemberId(lockCoinDetail.getMemberId());
                principalTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
                principalTransaction.setSymbol(lockCoinDetail.getCoinUnit());
                principalTransaction.setComment("SLB投资活动解锁（收益小于年利率--返还本金）");
                memberTransactionService.save(principalTransaction);

                //保存增加理财币种锁仓 本金资金记录
                MemberTransaction differenceTransaction = new MemberTransaction();
                differenceTransaction.setAmount(differenceCoinNum);
                differenceTransaction.setMemberId(lockCoinDetail.getMemberId());
                differenceTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
                differenceTransaction.setSymbol(lockCoinDetail.getCoinUnit());
                differenceTransaction.setComment("SLB投资活动解锁（收益小于年利率--返还差价）");
                memberTransactionService.save(differenceTransaction);

                //解冻锁仓余额
                MessageResult principalWalletResult = memberWalletService.thawBalanceFromLockBlance(memberWallet, principalCoinNum);
                if (principalWalletResult.getCode() != 0){
                    throw new IllegalArgumentException("可用余额不足");
                }
                //补差价
                MessageResult differenceWalletResult = memberWalletService.increaseBalance(memberWallet.getId(), differenceCoinNum);
                if (differenceWalletResult.getCode() != 0){
                    throw new IllegalArgumentException("可用余额不足");
                }
            }
            else {
                throw new IllegalArgumentException("==本金不小于实际返回佣金==");
            }
        }

        //修改锁仓记录信息
        lockCoinDetail.setUnlockTime(new Date());
        lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
        lockCoinDetail.setStatus(LockStatus.UNLOCKED);
        lockCoinDetail.setCancleTime(new Date());
        lockCoinDetailService.save(lockCoinDetail);

        //添加解锁记录
        UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
        unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
        unlockCoinDetail.setAmount(lockCoinDetail.getTotalAmount());
        unlockCoinDetail.setRemainAmount(lockCoinDetail.getRemainAmount());
        unlockCoinDetail.setPrice(coinPrice);
        unlockCoinDetail.setSettlementType(SettlementType.ACTIVITY_COIN);
        unlockCoinDetail.setUsdtPriceCNY(usdtPrice);
        unlockCoinDetail.setSettlementAmount(unlockCoinNum);
        unlockCoinDetail.setIncomeType(incomeType);
        save(unlockCoinDetail);
    }

    /**
     * 解锁内部锁仓活动
     * @author tansitao
     * @time 2018/8/7 14:32 
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlockInternalActivity(UnlockCoinTask unlockCoinTask, RestTemplate restTemplate){
        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(unlockCoinTask.getRefActivitieId());
        Assert.isTrue(lockCoinDetail != null,"内部锁仓记录不存在");
        LockCoinRechargeSetting lockCoinRechargeSetting = lockCoinRechargeSettingService.findOne(lockCoinDetail.getRefActivitieId());
        Assert.isTrue(lockCoinRechargeSetting != null,"内部锁仓配置不存在");
        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
        BigDecimal coinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinDetail.getCoinUnit());
        //如果价格为0，则说明价格异常
        if(coinPrice.compareTo(BigDecimal.ZERO) == 0){
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //获取USDT的人民币价格
        BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
        //如果价格为0，则说明价格异常
        if(usdtPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        BigDecimal unlockNum;
        //判断释放类型，是币价还是涨幅
        if(lockCoinRechargeSetting.getUnlockType() == LockCoinRechargeThresholdType.COIN_RANGE){
            //通过涨幅，计算解锁数量
            unlockNum = lockCoinDetail.getRemainAmount().multiply(lockCoinRechargeSetting.getUnlockValue());
        }else{
            unlockNum = lockCoinRechargeSetting.getUnlockValue();
        }
        //判断剩余锁仓数量是否大于解锁数量
        if(lockCoinDetail.getRemainAmount().compareTo(unlockNum) >= 0){
            //获取钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),lockCoinDetail.getMemberId());
            if(memberWallet == null) {
                log.error("=======解锁内部锁仓活动失败lockDetailId:" + lockCoinDetail.getId() + "memberId:" + lockCoinDetail.getMemberId() + "========无钱包" + lockCoinDetail.getCoinUnit());
                throw new IllegalArgumentException("钱包不存在");
            }
            //修改锁仓记录信息
            lockCoinDetail.setUnlockTime(new Date());
            lockCoinDetail.setRemainAmount(lockCoinDetail.getTotalAmount().subtract(unlockNum));
            lockCoinDetail.setStatus(LockStatus.UNLOCKED);
            lockCoinDetail.setCancleTime(new Date());
            lockCoinDetailService.save(lockCoinDetail);

            //添加解锁记录
            UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
            unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
            unlockCoinDetail.setAmount(unlockNum);
            unlockCoinDetail.setRemainAmount(lockCoinDetail.getRemainAmount());
            unlockCoinDetail.setPrice(coinPrice);
            unlockCoinDetail.setUsdtPriceCNY(usdtPrice);
            save(unlockCoinDetail);

            unlockCoinTask.setStatus(ProcessStatus.PROCESSED);
            unlockCoinTaskService.save(unlockCoinTask);

            //解冻锁仓余额
            MessageResult principalWalletResult = memberWalletService.thawBalanceFromLockBlance(memberWallet, unlockNum);
            if (principalWalletResult.getCode() != 0){
                throw new IllegalArgumentException("可用余额不足");
            }
        }

    }

    /**
      * 解锁节点产品
      * @author tansitao
      * @time 2018/7/31 11:44 
      */
    @Transactional(rollbackFor = Exception.class)
    public void unlockQuantify(LockCoinDetail lockCoinDetail, RestTemplate restTemplate) throws Exception
    {
        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
        BigDecimal coinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinDetail.getCoinUnit());
        //如果价格为0，则说明价格异常
        if(coinPrice.compareTo(BigDecimal.ZERO) == 0){
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //获取USDT的人民币价格
        BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
        //如果价格为0，则说明价格异常
        if(usdtPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //计算预计收益金额
        BigDecimal lockEarnings = lockCoinDetail.getTotalAmount().multiply(coinPrice).multiply(usdtPrice);
        //计算承诺收益
        BigDecimal lockPromiseEarnings = lockCoinDetail.getTotalCNY().add(lockCoinDetail.getPlanIncome());
        BigDecimal unlockCoinNum;
        SettlementType settlementType;
        IncomeType incomeType;
        //比较现在价格是否大于收益价格
        if(lockEarnings.compareTo(lockPromiseEarnings) >= 0){
            //币种涨幅大于预期收益，以币种解锁
            //设置收益类型，和收益总活动解锁币数
            incomeType = IncomeType.FINANCIAL_A1;
            settlementType = SettlementType.ACTIVITY_COIN;
            unlockCoinNum = lockCoinDetail.getTotalAmount();
            //获取钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(), lockCoinDetail.getMemberId());
            if(memberWallet == null) {
                log.error("=======解锁节点产品活动失败lockDetailId:" + lockCoinDetail.getId() + "memberId:" + lockCoinDetail.getMemberId() + "========无钱包" + lockCoinDetail.getCoinUnit());
                throw new IllegalArgumentException("钱包不存在");
            }

            //保存增加理财币种锁仓资金记录
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setAmount(unlockCoinNum);
            memberTransaction.setMemberId(lockCoinDetail.getMemberId());
            memberTransaction.setType(TransactionType.QUANTIFY_ACTIVITY);
            memberTransaction.setSymbol(lockCoinDetail.getCoinUnit());
            memberTransaction.setComment("节点产品活动解锁（收益大于年利率，" + lockCoinDetail.getCoinUnit() + "解锁）");
            memberTransactionService.save(memberTransaction);
            //解冻锁仓余额
            MessageResult walletResult = memberWalletService.thawBalanceFromLockBlance(memberWallet, unlockCoinNum);
            if (walletResult.getCode() != 0){
                throw new IllegalArgumentException("可用余额不足");
            }
        }
        else {
            //如果币价涨幅没有达到年化率，则用usdt解锁
            unlockCoinNum = lockPromiseEarnings.divide(usdtPrice,8,BigDecimal.ROUND_DOWN);
            //设置收益类型，和收益总USDT数
            incomeType = IncomeType.FINANCIAL_B1;
            settlementType = SettlementType.USDT_SETTL;
            //获取锁仓活动币种钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),lockCoinDetail.getMemberId());
            if(memberWallet == null) {
                log.error("=======解锁投资活动失败lockDetailId:" + lockCoinDetail.getId() + "memberId:" + lockCoinDetail.getMemberId() + "========无钱包" + lockCoinDetail.getCoinUnit());
                throw new IllegalArgumentException("钱包不存在");
            }
            //获取用户USDT钱包余额
            MemberWallet usdtMemberWallet = memberWalletService.findByCoinUnitAndMemberId("USDT", lockCoinDetail.getMemberId());
            if(usdtMemberWallet == null){
                throw new IllegalArgumentException("钱包不存在");
            }

            //解锁节点产品活动增加usdt余额的资金记录
            MemberTransaction unlockTransaction = new MemberTransaction();
            unlockTransaction.setAmount(unlockCoinNum);
            unlockTransaction.setMemberId(lockCoinDetail.getMemberId());
            unlockTransaction.setType(TransactionType.QUANTIFY_ACTIVITY);
            unlockTransaction.setSymbol("USDT");
            unlockTransaction.setComment("节点产品活动解锁（收益小于年利率--解锁增加USDT）");
            memberTransactionService.save(unlockTransaction);

            //解锁节点产品活动，减少活动币种锁仓余额
            MemberTransaction decreaseTransaction = new MemberTransaction();
            decreaseTransaction.setAmount(BigDecimal.ZERO.subtract(lockCoinDetail.getTotalAmount()));
            decreaseTransaction.setMemberId(lockCoinDetail.getMemberId());
            decreaseTransaction.setType(TransactionType.QUANTIFY_ACTIVITY);
            decreaseTransaction.setSymbol(lockCoinDetail.getCoinUnit());
            decreaseTransaction.setComment("节点产品活动解锁（收益小于年利率--扣除" + lockCoinDetail.getCoinUnit() + "锁仓余额）");
            memberTransactionService.save(decreaseTransaction);

            //减少锁仓币数
            MessageResult activityWalletResult = memberWalletService.decreaseLockBalance(memberWallet.getId(), lockCoinDetail.getTotalAmount());
            if (activityWalletResult.getCode() != 0){
                throw new IllegalArgumentException("可用余额不足");
            }
            MessageResult usdtWalletResult = memberWalletService.increaseBalance(usdtMemberWallet.getId(), unlockCoinNum);
            if(usdtWalletResult.getCode() != 0 ){
                throw new IllegalArgumentException("USDT添加余额失败");
            }
        }

        //修改锁仓记录信息
        lockCoinDetail.setUnlockTime(new Date());
        lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
        lockCoinDetail.setStatus(LockStatus.UNLOCKED);
        lockCoinDetail.setCancleTime(new Date());
        lockCoinDetailService.save(lockCoinDetail);

        //添加解锁记录
        UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
        unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
        unlockCoinDetail.setAmount(lockCoinDetail.getTotalAmount());
        unlockCoinDetail.setRemainAmount(lockCoinDetail.getRemainAmount());
        unlockCoinDetail.setPrice(coinPrice);
        unlockCoinDetail.setSettlementType(settlementType);
        unlockCoinDetail.setUsdtPriceCNY(usdtPrice);
        unlockCoinDetail.setSettlementAmount(unlockCoinNum);
        unlockCoinDetail.setIncomeType(incomeType);
        save(unlockCoinDetail);
    }


    public UnLockCoinDetailService getService(){return SpringContextUtil.getBean(this.getClass());}
}
