package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.constant.UttReleaseStatus;
import com.spark.bitrade.dao.LockUttReleasePlanDao;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.entity.LockUttReleasePlan;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.mapper.dao.LockUttReleasePlanMapper;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * UTT释放计划表 服务实现类
 * </p>
 *
 * @author qiliao
 * @since 2019-08-15
 */
@Service
@Slf4j
public class LockUttReleasePlanServiceImpl extends ServiceImpl<LockUttReleasePlanMapper, LockUttReleasePlan> implements LockUttReleasePlanService {

    @Autowired
    private LockUttReleasePlanDao lockUttReleasePlanDao;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private LockUttMemberService lockUttMemberService;
    @Autowired
    private MemberTransactionService transactionService;

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void doRelease(LockUttReleasePlan plan,TransactionType type) {
        log.info("=============================="+plan.getCoinUnit()+"解锁:{}====================================", plan.getId());
        Long memberId = plan.getMemberId();

        //钱包锁仓余额 增加到可用余额
        MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(plan.getCoinUnit(), plan.getMemberId());
        BigDecimal unlockAmount = plan.getUnlockAmount();
        BigDecimal lockBalance = wallet.getLockBalance();
        if (lockBalance.compareTo(unlockAmount) < 0) {
            unlockAmount = lockBalance;
            plan.setRemark(plan.getRemark() + "实际释放金额:" + unlockAmount.toPlainString());
        }

        //减少 lockcoindetail 剩余锁仓余额
        Long lockDetailId = plan.getLockDetailId();
        LockCoinDetail detail = lockCoinDetailService.findOne(lockDetailId);
        detail.setRemainAmount(detail.getRemainAmount().subtract(unlockAmount));
        if (detail.getRemainAmount().compareTo(BigDecimal.ZERO) <= 0) {
            detail.setStatus(LockStatus.UNLOCKED);
        }
        int i = lockCoinDetailService.updateRemainAmountAndLockStatus(detail.getRemainAmount(), detail.getStatus(), detail.getId());
        Assert.isTrue(i > 0, "减少剩余锁仓余额和改变状态失败");

        //修改 plan的状态
        plan.setStatus(UttReleaseStatus.RELEASED);
        plan.setUpdateTime(new Date());
        lockUttReleasePlanDao.save(plan);
        if (unlockAmount.compareTo(BigDecimal.ZERO) > 0) {
            //增加可用余额
            MessageResult inRes = memberWalletService.increaseBalance(wallet.getId(), unlockAmount);
            Assert.isTrue(inRes.isSuccess(), "增加余额失败..");
            //增加余额的交易流水
            MemberTransaction addTransaction = lockUttMemberService.creteTransactionNew(memberId, unlockAmount,
                    plan.getRemark() + "增加可用余额", plan.getId(), wallet.getAddress(), type,plan.getCoinUnit());
            transactionService.save(addTransaction);

            //减少锁仓余额
            MessageResult deRes = memberWalletService.decreaseLockBalance(wallet.getId(), unlockAmount);
            Assert.isTrue(deRes.isSuccess(), "减少锁仓余额失败..");
        }
        log.info("==============================UTT解锁成功:{}====================================", plan.getId());
    }
}
