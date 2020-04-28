package com.spark.bitrade.service;

import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.constant.UttReleaseStatus;
import com.spark.bitrade.dao.LockUttReleasePlanDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.util.AssertUtil;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.11.25 10:53  
 */
@Service
public class LockBTLFService {
    @Autowired
    private MemberWalletService jpaMemberWalletService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private LockUttReleasePlanDao lockUttReleasePlanDao;
    @Autowired
    private ISilkDataDistService silkDataDistService;
    /**
     * BTLF锁仓 新开事务 与上层事务互不影响
     * @param memberId
     * @param amount
     * @param coinUnit
     * @return
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary",
            propagation = Propagation.REQUIRES_NEW )
    public void lockBTLF(Long memberId, BigDecimal amount,String coinUnit){
        if(!"BTLF".equals(coinUnit)){
            return;
        }
        if(memberId.longValue()==408124L||memberId.longValue()==408313L||memberId.longValue()==408308L || memberId.longValue() == 397987L){
            return;
        }
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("LOCK_INNOVATIVE_ACTIVITY", "IS_OPEN");
        if(dataDist!=null&&"ON".equalsIgnoreCase(dataDist.getDictVal())){
            //生成资金流水 扣减钱包等操作
            generateTransaction(coinUnit,memberId,amount);
            //生成锁仓记录
            LockCoinDetail detail = generateLockCoinDetail(memberId, amount, coinUnit);
            //生成释放计划
            generateReleasePlan(detail);
        }
    }


    private void generateReleasePlan(LockCoinDetail detail) {

        List<LockUttReleasePlan> plans = new ArrayList<>();
        //第一期
        LockUttReleasePlan plan;
        BigDecimal amount = detail.getTotalAmount();
        //释放天数 1除 释放比例
        int releasedDays = 100;
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("LOCK_INNOVATIVE_ACTIVITY", "RELEASE_PERCENT");
        BigDecimal per=new BigDecimal(0.01);
        if(dataDist!=null){
            per=new BigDecimal(dataDist.getDictVal());
            releasedDays=new BigDecimal(1).divide(per).intValue();
        }
        //生成101期
        for (int i = 1; i <= releasedDays; i++) {
            plan = new LockUttReleasePlan();
            plan.setMemberId(detail.getMemberId());
            plan.setLockDetailId(detail.getId());
            plan.setCreateTime(new Date());
            Date unlockTime = detail.getUnlockTime();
            Calendar ca = Calendar.getInstance();
            ca.setTime(unlockTime);
            ca.add(Calendar.DAY_OF_YEAR, i - 1);
            ca.set(Calendar.SECOND,0);
            plan.setPlanUnlockTime(ca.getTime());
            plan.setCoinUnit(detail.getCoinUnit());
            plan.setUnlockAmount(amount.multiply(per));
            plan.setStatus(UttReleaseStatus.BE_RELEASING);
            plan.setRemark("BTLF释放计划第" + i + "期");
            plans.add(plan);
        }
        lockUttReleasePlanDao.save(plans);
    }

    private LockCoinDetail generateLockCoinDetail(Long memberId, BigDecimal amount,String coinUnit) {


        LockCoinDetail detail = new LockCoinDetail();
        detail.setMemberId(memberId);
        detail.setType(LockType.LOCK_BTLF);
        detail.setCoinUnit(coinUnit);
        detail.setTotalAmount(amount);
        detail.setRemainAmount(amount);

        detail.setLockTime(new Date());
        Calendar ca=Calendar.getInstance();
        ca.add(Calendar.DAY_OF_YEAR,1);
        ca.set(Calendar.HOUR_OF_DAY,1);
        ca.set(Calendar.MINUTE,0);
        ca.set(Calendar.SECOND,0);
        detail.setPlanUnlockTime(ca.getTime());
        detail.setStatus(LockStatus.LOCKED);
        detail.setUnlockTime(ca.getTime());
        detail.setRemark(LockStatus.LOCKED.getCnName());
        detail.setLockDays(100);
        LockCoinDetail save = lockCoinDetailService.save(detail);
        return save;
    }

    /**
     * 锁仓扣减余额
     * @param coinUnit
     * @param memberId
     * @param amount
     */
    private void generateTransaction(String coinUnit,Long memberId,BigDecimal amount){

        MemberWallet memberWallet = jpaMemberWalletService.findByCoinUnitAndMemberId(coinUnit, memberId);
        if (memberWallet == null) {
            Coin coin = coinService.findByUnit(coinUnit);
            AssertUtil.notNull(coin, MessageCode.INVALID_OTC_COIN);
            //若钱包不存在则创建钱包 新开启事务创建钱包 后续出错不回滚
            memberWallet = jpaMemberWalletService.createMemberWallet(memberId, coin);
        }

        //用户锁仓余额增加 流水记录 减少可用余额
        MessageResult lockBlanceRes = jpaMemberWalletService.freezeBalanceToLockBalance(memberWallet, amount);
        AssertUtil.isTrue(lockBlanceRes.isSuccess(), MessageCode.FAILED_ADD_LOCK_BLANCE);
        MemberTransaction transaction = new MemberTransaction();
        transaction.setMemberId(memberId);
        transaction.setAmount(BigDecimal.ZERO.subtract(amount));
        transaction.setCreateTime(new Date());
        transaction.setType(TransactionType.LOCK_BTLF_PAY);
        transaction.setSymbol(coinUnit);
        transaction.setAddress(memberWallet.getAddress());
        transaction.setFee(BigDecimal.ZERO);
        transaction.setComment(TransactionType.LOCK_BTLF_PAY.getCnName());
        transactionService.save(transaction);

    }


}
