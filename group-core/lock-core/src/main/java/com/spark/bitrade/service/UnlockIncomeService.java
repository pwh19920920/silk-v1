package com.spark.bitrade.service;

import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dao.UnLockCoinDetailDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PostRemove;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * cnyt锁仓收益解锁
 * @author Zhang Yanjun
 * @time 2018.12.11 17:56
 */
@Service
@Slf4j
public class UnlockIncomeService extends BaseService{

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private LockMemberIncomePlanService lockMemberIncomePlanService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private UnLockCoinDetailDao unLockCoinDetailDao;

    @Autowired
    private CoinService coinService;

    public UnlockCoinDetail save(UnlockCoinDetail unlockCoinDetail){
        return unLockCoinDetailDao.save(unlockCoinDetail);
    }

    /**
     * 以活动币种的方式解锁用户锁仓收益
     * @author Zhang Yanjun
     * @time 2018.12.11 17:58
     */
    @Transactional
    public void unlockFinanCoinByActCoin(LockMemberIncomePlan lockMemberIncomePlan)throws Exception{
        //锁仓币种
        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(lockMemberIncomePlan.getLockDetailId());
        //钱包信息
        Coin coin = coinService.findByUnit(lockCoinDetail.getCoinUnit());
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(), lockMemberIncomePlan.getMemberId());
        if (memberWallet == null){
            //如果不存在，则创建一条记录
            memberWallet = memberWalletService.createMemberWallet(lockCoinDetail.getMemberId(), coin);
        }
        //可用余额增加奖励金额
        MessageResult result = memberWalletService.increaseBalance(memberWallet.getId(), lockMemberIncomePlan.getAmount());
        if (result.getCode() != 0){
            throw new IllegalArgumentException("可用余额不足");
        }
        //交易记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setMemberId(lockMemberIncomePlan.getMemberId());
        memberTransaction.setCreateTime(new Date());
        memberTransaction.setSymbol(lockCoinDetail.getCoinUnit());
        memberTransaction.setComment("cnyt锁仓活动（用户锁仓收益）解锁-第"+lockMemberIncomePlan.getPeriod()+"期");
        memberTransaction.setType(TransactionType.STO_ACTIVITY);
        memberTransaction.setAmount(lockMemberIncomePlan.getAmount());
        memberTransactionService.save(memberTransaction);

        //返还计划 改为已返还
        lockMemberIncomePlanService.updateStatus(lockMemberIncomePlan.getId(), LockBackStatus.BACKED, LockBackStatus.BACKING);
    }

    /**
     * 以活动币种的方式解锁用户锁仓本金
     * @author Zhang Yanjun
     * @time 2018.12.11 17:58
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlockCorByActCoin(LockCoinDetail lockCoinDetail)throws Exception{
        //钱包信息
        MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),lockCoinDetail.getMemberId());
        Coin coin = coinService.findByUnit(lockCoinDetail.getCoinUnit());
        if (wallet == null){
            //如果不存在，则创建一条记录
            wallet = memberWalletService.createMemberWallet(lockCoinDetail.getMemberId(), coin);
        }
        //锁仓转余额
        MessageResult result = memberWalletService.thawBalanceFromLockBlance(wallet,lockCoinDetail.getTotalAmount());
        if (result.getCode() != 0){
            throw new IllegalArgumentException("可用余额不足");
        }
        //修改锁仓信息
        lockCoinDetail.setUnlockTime(new Date());
        lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
        lockCoinDetail.setStatus(LockStatus.UNLOCKED);
        lockCoinDetailService.save(lockCoinDetail);

        //添加解锁记录
        UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
        unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
        unlockCoinDetail.setAmount(lockCoinDetail.getTotalAmount());
        unlockCoinDetail.setRemainAmount(lockCoinDetail.getRemainAmount());
        save(unlockCoinDetail);

        //添加资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(lockCoinDetail.getTotalAmount());
        memberTransaction.setMemberId(lockCoinDetail.getMemberId());
        memberTransaction.setType(TransactionType.STO_ACTIVITY);
        memberTransaction.setSymbol(lockCoinDetail.getCoinUnit());
        memberTransaction.setComment(lockCoinDetail.getCoinUnit()+"锁仓活动（用户锁仓本金）解锁");
        memberTransactionService.save(memberTransaction);

    }

}
