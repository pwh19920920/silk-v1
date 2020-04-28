package com.spark.bitrade.service.impl;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.LockBccAssignRecordDao;
import com.spark.bitrade.dao.LockBccAssignUnlockDao;
import com.spark.bitrade.dao.LockCoinDetailDao;
import com.spark.bitrade.dao.LockIeoRestitutionIncomePlanDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vo.LockBccLockedInfoVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class LockBccAssignRecordServiceImpl implements LockBccAssignRecordService {

    /**
     * 一次查询ieo释放计划限制数
     */
    private static final Integer QUERY_UNLOCK_IEO_NUM = 500;

    @Autowired
    private LockCoinDetailDao lockCoinDetailDao;

    @Autowired
    private LockIeoRestitutionIncomePlanDao lockIeoRestitutionIncomePlanDao;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private LockBccAssignRecordDao lockBccAssignRecordDao;

    @Autowired
    private LockBccAssignUnlockDao lockBccAssignUnlockDao;

    @Autowired
    private CoinService coinService;

    @Override
    public void bccLock(LockCoinActivitieSetting setting, Member member, BigDecimal coinCnyPrice,
                        BigDecimal coinUSDTPrice, BigDecimal usdtPrice, BigDecimal amount, Integer portion) {
        // 查询记录
        LockBccAssignRecord lockBccAssignRecord = lockBccAssignRecordDao.findByMemberId(member.getId());
        // 锁仓记录存在
        if (lockBccAssignRecord != null) {
            int maxBuy = setting.getMaxBuyAmount().divide(setting.getUnitPerAmount(), 0, BigDecimal.ROUND_UP).intValue();
            //先判断
            Assert.isTrue(lockBccAssignRecord.getReleasePortion() + portion * 2 <= maxBuy * 2, "超出最大可锁仓份数");
            // 判断能否继续锁仓
            Assert.isTrue((lockBccAssignRecord.getLockPortion() % maxBuy == 0 && lockBccAssignRecord.getReleaseAmount().compareTo(BigDecimal.ZERO) == 0)
                    || lockBccAssignRecord.getLockPortion() % maxBuy > 0, "锁仓已达上限，详见活动规则或咨询客服");
        }
        // 锁仓不存在，则新建
        if (lockBccAssignRecord == null) {
            lockBccAssignRecord = new LockBccAssignRecord();
        }

        // 记录锁仓详情
        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        // 保存锁仓记录
        getService().saveBccLockDetailRecord(lockCoinDetail, member, setting, portion,
                coinCnyPrice, amount, usdtPrice, lockBccAssignRecord);
        // 计算上级收益
        getService().updateLockBccAssignRecord(lockCoinDetail, setting, lockBccAssignRecord);

    }

    /**
     * 保存bcc锁仓详情记录
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void saveBccLockDetailRecord(LockCoinDetail lockCoinDetail, Member member, LockCoinActivitieSetting setting, Integer portion,
                                        BigDecimal coinCnyPrice, BigDecimal amount, BigDecimal usdtPrice, LockBccAssignRecord lockBccAssignRecord) {
        // 开始解锁时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, setting.getBeginDays());

        lockCoinDetail.setMemberId(member.getId());
        lockCoinDetail.setType(LockType.ENERGIZE);
        lockCoinDetail.setCoinUnit(setting.getCoinSymbol());
        lockCoinDetail.setRefActivitieId(setting.getId());
        lockCoinDetail.setTotalAmount(amount);
        lockCoinDetail.setLockPrice(coinCnyPrice);
        lockCoinDetail.setRemainAmount(amount);
        lockCoinDetail.setLockTime(new Date());
        lockCoinDetail.setPlanUnlockTime(calendar.getTime());
        lockCoinDetail.setPlanIncome(BigDecimal.ZERO);
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setUnlockTime(calendar.getTime());
        lockCoinDetail.setUsdtPriceCNY(usdtPrice);
        lockCoinDetail.setTotalCNY(coinCnyPrice.multiply(amount));
        lockCoinDetail.setRemark(String.format("BCC赋能锁仓：%f", amount));
        lockCoinDetail.setLockDays(setting.getLockDays());
        lockCoinDetail.setEarningRate(BigDecimal.ZERO);
        lockCoinDetail.setUnitPerAmount(BigDecimal.ONE);
        lockCoinDetail.setLockRewardSatus(LockRewardSatus.DEFAULT_REWARD);
        lockCoinDetail.setSmsSendStatus(SmsSendStatus.NO_SMS_SEND);
        lockCoinDetail.setLockCycle(setting.getLockCycle());
        lockCoinDetail.setBeginDays(setting.getBeginDays());
        lockCoinDetail.setCycleDays(setting.getCycleDays());
        lockCoinDetail.setCycleRatio(setting.getCycleRatio());

        lockCoinDetail = lockCoinDetailDao.save(lockCoinDetail);
        log.info("新增会员{}锁仓详情记录成功", member.getId());

        // 冻结钱包余额
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(setting.getCoinSymbol(), member.getId());
        Assert.isTrue(memberWallet != null, "用户BCC钱包不存在");
        Assert.isTrue(memberWallet.getBalance().compareTo(amount) >= 0, "可用余额不足");

        MessageResult decreaseResult = memberWalletService.decreaseBalance(memberWallet.getId(), amount);
        if (decreaseResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        //锁仓冻结钱包余额
        MessageResult freezeResult = memberWalletService.increaseLockBalance(memberWallet.getId(), amount);
        if (freezeResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        // 冻结锁定金额
        log.info("冻结会员{}锁仓金额成功", member.getId());
        // 保存BCC扣除资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setMemberId(member.getId());
        memberTransaction.setAmount(BigDecimal.ZERO.subtract(amount));
        memberTransaction.setType(TransactionType.ENERGIZE_LOCK);
        memberTransaction.setSymbol(setting.getCoinSymbol());
        memberTransaction.setRefId(String.valueOf(lockCoinDetail.getId()));
        memberTransactionService.save(memberTransaction);
        // 冻结锁定金额
        log.info("保存会员{}BCC扣除资金交易记录成功", member.getId());

        // bcc赋能锁仓详情不存在，则新建
        if (lockBccAssignRecord.getId() == null) {
            lockBccAssignRecord.setActivityId(setting.getId());
            lockBccAssignRecord.setMemberId(member.getId());
            lockBccAssignRecord.setSuperiorId(member.getInviterId());
            lockBccAssignRecord.setLockPortion(portion);
            lockBccAssignRecord.setLockAmount(amount);
            lockBccAssignRecord.setCommissionReward(amount.multiply(new BigDecimal(2)));
            lockBccAssignRecord.setRewardPortion(portion * 2);
            lockBccAssignRecord.setReleasePortion(portion * 2);
            lockBccAssignRecord.setRewardAmount(amount.multiply(new BigDecimal(2)));
            lockBccAssignRecord.setReleaseAmount(amount.multiply(new BigDecimal(2)));
            lockBccAssignRecord.setStatus(BooleanEnum.IS_TRUE);
            lockBccAssignRecord.setComment("BCC赋能锁仓");
            lockBccAssignRecord.setCreateTime(new Date());
            lockBccAssignRecordDao.save(lockBccAssignRecord);
        }
        // 存在记录，更新
        else {
            lockBccAssignRecord.setLockPortion(lockBccAssignRecord.getLockPortion() + portion);
            lockBccAssignRecord.setLockAmount(amount.add(lockBccAssignRecord.getLockAmount()));
            lockBccAssignRecord.setCommissionReward(lockBccAssignRecord.getCommissionReward().add(amount.multiply(new BigDecimal(2))));
            lockBccAssignRecord.setRewardPortion(lockBccAssignRecord.getRewardPortion() + portion * 2);
            lockBccAssignRecord.setReleasePortion(lockBccAssignRecord.getReleasePortion() + portion * 2);
            lockBccAssignRecord.setRewardAmount(lockBccAssignRecord.getRewardAmount().add(amount.multiply(new BigDecimal(2))));
            lockBccAssignRecord.setReleaseAmount(lockBccAssignRecord.getReleaseAmount().add(amount.multiply(new BigDecimal(2))));
            lockBccAssignRecord.setUpdateTime(new Date());
            lockBccAssignRecordDao.saveAndFlush(lockBccAssignRecord);
        }
        log.info("更新会员{}赋能锁仓详情成功", member.getId());
    }

    /**
     * 更新锁仓记录
     *
     * @param lockCoinDetail
     * @return
     */
    public void updateLockBccAssignRecord(LockCoinDetail lockCoinDetail, LockCoinActivitieSetting setting, LockBccAssignRecord lockBccAssignRecord) {

        // 计算上级返佣
        if (lockBccAssignRecord.getSuperiorId() != null) {
            getService().computeSuperiorReward(lockBccAssignRecord.getSuperiorId(), setting, lockCoinDetail);
        }
    }

    /**
     * 异步计算上级返佣
     */
    @Async
    public void computeSuperiorReward(Long superiorId, LockCoinActivitieSetting setting, LockCoinDetail lockCoinDetail) {
        log.info("=====================开始计算会员{}bcc赋能返佣====================", superiorId);
        // 上级子级锁仓总次数
        Integer subLockPortion = lockBccAssignRecordDao.findSubLockPortion(superiorId);

        // 上级bcc赋能锁仓详情
        LockBccAssignRecord superiorLockBccAssignRecord = lockBccAssignRecordDao.findByMemberId(superiorId);

        // 会员 ieo 锁仓条数
        List<LockIeoRestitutionIncomePlan> lockIeoRestitutionIncomePlans = lockIeoRestitutionIncomePlanDao.getLockIeoRestitutionIncomePlanList(superiorId, 1);
        // 会员 bcc赋能 ieo 已解份数
        List<LockBccAssignUnlock> lockBccAssignUnlocks = lockBccAssignUnlockDao.findIeoLockBccAssignUnlock(superiorId);
        // 没有参加bcc赋能锁仓，也没有参加ieo 锁仓，直接返回
        if (superiorLockBccAssignRecord == null && lockIeoRestitutionIncomePlans.size() == 0) {
            return;
        }
        // 理应释放份数
        Integer rewardPortion;
        // 无ieo锁仓记录，只释放佣金
        if (superiorLockBccAssignRecord != null && lockIeoRestitutionIncomePlans.size() == 0) {
            // 理应释放份数 = 下级参投份数 / 3  - (累计赠送份数 - 赠送待释份数) - ieo释放份数
            rewardPortion = subLockPortion / 3
                    - (superiorLockBccAssignRecord.getRewardPortion() - superiorLockBccAssignRecord.getReleasePortion())
                    - lockBccAssignUnlocks.size();
            if (rewardPortion > 0) {
                getService().unlockCommission(superiorLockBccAssignRecord, superiorId, setting, lockCoinDetail, rewardPortion);
            }
        }
        // 释放佣金 同时释放ieo锁仓
        else if (superiorLockBccAssignRecord != null && lockIeoRestitutionIncomePlans.size() > 0) {
            // 理应释放份数 = 下级参投份数 / 3  - (累计赠送份数 - 赠送待释份数) - ieo释放份数
            rewardPortion = subLockPortion / 3
                    - (superiorLockBccAssignRecord.getRewardPortion() - superiorLockBccAssignRecord.getReleasePortion())
                    - lockBccAssignUnlocks.size();
            // ieo 应释放份数
            Integer releaseIeoPortion = 0;
            if (rewardPortion > 0) {
                releaseIeoPortion = rewardPortion - getService().unlockCommission(superiorLockBccAssignRecord, superiorId, setting, lockCoinDetail, rewardPortion);
            }

            if (releaseIeoPortion > 0) {
                log.info("=================开始释放会员{}bcc赋能ieo锁仓===============", superiorLockBccAssignRecord.getMemberId());
                getService().unlockIeoLocked(setting, releaseIeoPortion, superiorId);
            }

        }
        // 佣金不存在，只释放ieo锁仓
        else if (superiorLockBccAssignRecord == null && lockIeoRestitutionIncomePlans.size() > 0) {
            // 没有参加bcc赋能锁仓,理应释放份数 = 下级参投份数 / 3  - (累计赠送份数 - 赠送待释份数) - ieo释放份数
            rewardPortion = subLockPortion / 3 - lockBccAssignUnlocks.size();
            log.info("=================开始释放会员{}bcc赋能ieo锁仓===============", superiorId);
            getService().unlockIeoLocked(setting, rewardPortion, superiorId);
        }
    }

    /**
     * 释放ieo锁仓
     *
     * @param setting
     * @param rewardPortion
     * @param superiorId
     */
    public void unlockIeoLocked(LockCoinActivitieSetting setting, Integer rewardPortion,
                                Long superiorId) {
        // 锁仓总和
        BigDecimal lockAmount = lockIeoRestitutionIncomePlanDao.getRestitutionIncomePlanAmount(superiorId);
        // 理应释放数量
        BigDecimal rewardAmount = setting.getUnitPerAmount().multiply(new BigDecimal(rewardPortion));
        // 实际应释放数量
        BigDecimal releaseAmount = rewardAmount.compareTo(lockAmount) >= 0 ? lockAmount : rewardAmount;

        // 实际应释放数量为0，直接返回
        if (releaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("=================会员{}bcc赋能ieo加速释放可释放数为0,释放结束===============", superiorId);
            return;
        }
        // 已释放数量
        BigDecimal releasingAmount = BigDecimal.ZERO;
        // 解锁对应 实际应释放数量 的ieo锁仓
        getService().findLockingRecordsToUnlock(superiorId, releaseAmount, releasingAmount, setting);
    }

    /**
     * 查出可解ieo记录, 进行解锁
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void findLockingRecordsToUnlock(Long superiorId, BigDecimal releaseAmount, BigDecimal releasingAmount, LockCoinActivitieSetting setting) {
        // 会员 ieo 锁仓条数
        List<LockIeoRestitutionIncomePlan> lockIeoRestitutionIncomePlans = lockIeoRestitutionIncomePlanDao.getLockIeoRestitutionIncomePlanList(superiorId, QUERY_UNLOCK_IEO_NUM);

        // 解锁制定条数的ieo锁仓记录
        ReleaseResultObject releaseResult = getService().doRelease(lockIeoRestitutionIncomePlans, releaseAmount, releasingAmount, setting);
        // 已经释放完
        if (releaseResult.getResult()) {
            // 增加 BCC赋能佣金解锁记录，每一份产生一条记录
            releasingAmount = releaseResult.getReleasingAmount();

//            // 减少用户 锁仓余额 modify by qhliao 减少余额放到里面
//            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(setting.getCoinSymbol(), superiorId);
//            MessageResult result = memberWalletService.decreaseLockBalance(memberWallet.getId(), releasingAmount);
//            if (result.getCode() != 0) {
//                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
//            }

            while (releasingAmount.subtract(setting.getUnitPerAmount()).compareTo(setting.getUnitPerAmount().negate()) > 0) {
                LockBccAssignUnlock lockBccAssignUnlock = new LockBccAssignUnlock();
                lockBccAssignUnlock.setActivityId(setting.getId());
                lockBccAssignUnlock.setComment("BCC赋能ieo解锁");
                lockBccAssignUnlock.setMemberId(superiorId);
                // 当剩余的数量不足一份时，将剩下的数量作为释放的记录的释放数量
                lockBccAssignUnlock.setReleasedAmount(releasingAmount.subtract(setting.getUnitPerAmount()).compareTo(BigDecimal.ZERO) < 0
                        ? releasingAmount : setting.getUnitPerAmount());
                lockBccAssignUnlock.setReleaseType(LockBccAssignUnlockTypeEnum.IEO);
                lockBccAssignUnlock.setReleaseTime(new Date());
                lockBccAssignUnlockDao.save(lockBccAssignUnlock);
                // 修改 实际应释放数量，用于判断
                releasingAmount = releasingAmount.subtract(setting.getUnitPerAmount());
            }
            log.info("=================释放会员{}bcc赋能ieo锁仓成功===============", superiorId);
            return;
        }
        // 还有剩余记录为解锁
        if (lockIeoRestitutionIncomePlans.size() == QUERY_UNLOCK_IEO_NUM) {
            getService().findLockingRecordsToUnlock(superiorId, releaseAmount, releasingAmount, setting);
        }
    }

    /**
     * 解锁制定数量的记录
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public ReleaseResultObject doRelease(List<LockIeoRestitutionIncomePlan> lockIeoRestitutionIncomePlans,
                                         BigDecimal releaseAmount, BigDecimal releasingAmount, LockCoinActivitieSetting setting) {
        ReleaseResultObject releaseResultObject = new ReleaseResultObject();
        for (LockIeoRestitutionIncomePlan lockIeoRestitutionIncomePlan : lockIeoRestitutionIncomePlans) {
            try {
                //判断已释放数量是否 达到 实际应释放数量
                if (releasingAmount.add(lockIeoRestitutionIncomePlan.getRestitutionAmount()).compareTo(releaseAmount) > 0) {
                    // 即将达到实际应释放数量，计算 已释放数量 与 实际应释放数量的差值，最为下一条释放的释放部分
                    BigDecimal releasePart = releaseAmount.subtract(releasingAmount);

                    // 差值为0，已释放数量 已到达实际应释放数量，解仓结束
                    if (releasePart.compareTo(BigDecimal.ZERO) == 0) {
                        releaseResultObject.setResult(true);
                        releaseResultObject.setReleasingAmount(releasingAmount);
                        return releaseResultObject;
                    }
                    releasingAmount = getService().doSingleReleasePart(lockIeoRestitutionIncomePlan, setting, releasingAmount, releasePart);
                    releaseResultObject.setResult(true);
                    releaseResultObject.setReleasingAmount(releasingAmount);

                } else {
                    // ieo待解记录返还
                    releasingAmount = getService().doSingleRelease(lockIeoRestitutionIncomePlan, setting, releasingAmount);
                }
            } catch (Exception e) {
                log.info("bcc ieo解仓记录id:{}解锁失败", lockIeoRestitutionIncomePlan.getId());
            }
        }
        releaseResultObject.setResult(true);
        releaseResultObject.setReleasingAmount(releasingAmount);
        return releaseResultObject;
    }

    /**
     * 解锁一条记录的部分，状态不改变
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public BigDecimal doSingleReleasePart(LockIeoRestitutionIncomePlan lockIeoRestitutionIncomePlan, LockCoinActivitieSetting setting,
                                          BigDecimal releasingAmount, BigDecimal releasePart) {
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(releasePart);
        memberTransaction.setMemberId(lockIeoRestitutionIncomePlan.getMemberId());
        memberTransaction.setType(TransactionType.ENERGIZE_LOCK);
        memberTransaction.setSymbol(setting.getCoinSymbol());
        memberTransaction.setRefId(lockIeoRestitutionIncomePlan.getId() + "");
        memberTransaction.setComment("BCC赋能IEO加速释放");
        memberTransactionService.save(memberTransaction);

        //获取钱包
        MemberWallet memberWallet = memberWalletService.findCacheByCoinUnitAndMemberId(setting.getCoinSymbol(), lockIeoRestitutionIncomePlan.getMemberId());
        //减少锁仓余额
        MessageResult result = memberWalletService.decreaseLockBalance(memberWallet.getId(), releasePart);
        if (result.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        //增加用户钱包余额
        memberWalletService.increaseBalance(memberWallet.getId(), releasePart);

        // 该条记录释放一部分，更新待释放数量，状态不变
        lockIeoRestitutionIncomePlan.setRestitutionAmount(lockIeoRestitutionIncomePlan.getRestitutionAmount().subtract(releasePart));
        lockIeoRestitutionIncomePlan.setUpdateTime(new Date());
        lockIeoRestitutionIncomePlanDao.saveAndFlush(lockIeoRestitutionIncomePlan);

        // 改变累计释放数量
        releasingAmount = releasingAmount.add(releasePart);
        return releasingAmount;
    }

    /**
     * 解锁单条ieo待返回记录，状态改变
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public BigDecimal doSingleRelease(LockIeoRestitutionIncomePlan lockIeoRestitutionIncomePlan,
                                      LockCoinActivitieSetting setting, BigDecimal releasingAmount) {
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(lockIeoRestitutionIncomePlan.getRestitutionAmount());
        memberTransaction.setMemberId(lockIeoRestitutionIncomePlan.getMemberId());
        memberTransaction.setType(TransactionType.ENERGIZE_LOCK);
        memberTransaction.setSymbol(setting.getCoinSymbol());
        memberTransaction.setRefId(lockIeoRestitutionIncomePlan.getId() + "");
        memberTransaction.setComment("BCC赋能IEO加速释放");
        memberTransactionService.save(memberTransaction);

        //获取钱包
        MemberWallet memberWallet = memberWalletService.findCacheByCoinUnitAndMemberId(setting.getCoinSymbol(), lockIeoRestitutionIncomePlan.getMemberId());
        //减少锁仓余额
        MessageResult result = memberWalletService.decreaseLockBalance(memberWallet.getId(), lockIeoRestitutionIncomePlan.getRestitutionAmount());
        if (result.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        //增加用户钱包余额
        memberWalletService.increaseBalance(memberWallet.getId(), lockIeoRestitutionIncomePlan.getRestitutionAmount());

        int updateResult = lockIeoRestitutionIncomePlanDao.updateStatus(lockIeoRestitutionIncomePlan.getId(),
                LockBackStatus.BACK, LockBackStatus.BACKED);
        Assert.isTrue(updateResult > 0, "更新ieo解仓记录失败");
        // 改变累计释放数量
        releasingAmount = releasingAmount.add(lockIeoRestitutionIncomePlan.getRestitutionAmount());

        return releasingAmount;
    }


    /**
     * 释放佣金
     *
     * @param superiorLockBccAssignRecord
     * @param superiorId
     * @param setting
     * @param lockCoinDetail
     * @param rewardPortion
     * @return 实际释放了的佣金份数
     * @author fatKarin
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public Integer unlockCommission(LockBccAssignRecord superiorLockBccAssignRecord, Long superiorId,
                                    LockCoinActivitieSetting setting, LockCoinDetail lockCoinDetail, Integer rewardPortion) {
        log.info("=================开始释放会员{}bcc赋能佣金===============", superiorLockBccAssignRecord.getMemberId());
        // 统一数据库记录时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // bcc赋能锁仓详情实际待释放份数，判断待释放份数 是否大于 理应返佣份数
        Integer releaseCommissionPortion = superiorLockBccAssignRecord.getReleasePortion() >= rewardPortion
                ? rewardPortion : superiorLockBccAssignRecord.getReleasePortion();
        // bcc赋能锁仓详情实际待释放份数等于0
        if (releaseCommissionPortion.equals(0)) {
            return releaseCommissionPortion;
        }

        // 赠送数量
        BigDecimal releaseCommissionAmount = setting.getUnitPerAmount().multiply(new BigDecimal(releaseCommissionPortion));

        // 更新bcc赋能锁仓详情
        lockBccAssignRecordDao.updateByMemberId(superiorLockBccAssignRecord.getId(), releaseCommissionPortion, releaseCommissionAmount);
        log.info("BCC佣金释放:更新会员{}bcc赋能锁仓详情记录成功", superiorLockBccAssignRecord.getMemberId());

        // 增加 BCC赋能佣金解锁记录
        LockBccAssignUnlock lockBccAssignUnlock = new LockBccAssignUnlock();
        lockBccAssignUnlock.setActivityId(setting.getId());
        lockBccAssignUnlock.setComment("BCC赋能佣金解锁");
        lockBccAssignUnlock.setMemberId(superiorId);
        lockBccAssignUnlock.setReleasedAmount(setting.getUnitPerAmount().multiply(new BigDecimal(releaseCommissionPortion)));
        lockBccAssignUnlock.setReleaseType(LockBccAssignUnlockTypeEnum.COMMISSION);
        lockBccAssignUnlock.setReleaseTime(calendar.getTime());

        lockBccAssignUnlockDao.save(lockBccAssignUnlock);
        log.info("BCC佣金释放:新增会员{}BCC赋能佣金解锁记录,解锁方式为{}", lockBccAssignUnlock.getMemberId(), lockBccAssignUnlock.getReleaseType().getCnName());

        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(setting.getCoinSymbol(), superiorId);
        Assert.notNull(memberWallet, "未找到用户钱包");

        // 增加用户钱包 余额
        MessageResult activityWalletResult = memberWalletService.increaseBalance(memberWallet.getId(), releaseCommissionAmount);
        if (activityWalletResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        // 保存BCC佣金释放资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setMemberId(superiorId);
        memberTransaction.setAmount(releaseCommissionAmount);
        memberTransaction.setType(TransactionType.ENERGIZE_LOCK);
        memberTransaction.setSymbol(setting.getCoinSymbol());
        memberTransaction.setRefId(String.valueOf(lockCoinDetail.getId()));
        memberTransactionService.save(memberTransaction);

        return releaseCommissionPortion;
    }

    @Override
    public LockBccLockedInfoVo findLockBccLockedInfo(Long memberId, LockCoinActivitieSetting setting) {
        LockBccLockedInfoVo lockBccLockedInfoVo = new LockBccLockedInfoVo();

        LockBccAssignRecord lockBccAssignRecord = lockBccAssignRecordDao.findByMemberId(memberId);

        int maxBuy = setting.getMaxBuyAmount().divide(setting.getUnitPerAmount(), 0, BigDecimal.ROUND_UP).intValue();

        if (lockBccAssignRecord == null) {
            lockBccLockedInfoVo.setLockedAmount(BigDecimal.ZERO);
            lockBccLockedInfoVo.setUnlockAmount(BigDecimal.ZERO);
            lockBccLockedInfoVo.setAdmitLockAmount(setting.getUnitPerAmount().multiply(new BigDecimal(maxBuy)));
        } else {
            lockBccLockedInfoVo.setLockedAmount(lockBccAssignRecord.getLockAmount());
            lockBccLockedInfoVo.setUnlockAmount(lockBccAssignRecord.getReleaseAmount());

            //add by qhliao 判断能否继续锁仓 如果能则计算 否则为0
            if ((lockBccAssignRecord.getLockPortion() % maxBuy == 0 && lockBccAssignRecord.getReleaseAmount().compareTo(BigDecimal.ZERO) == 0)
                    || lockBccAssignRecord.getLockPortion() % maxBuy > 0) {
                // 计算可锁份数
                Integer lockedPortion = lockBccAssignRecord.getLockPortion() % maxBuy == 0 ?
                         maxBuy : maxBuy - lockBccAssignRecord.getLockPortion() % maxBuy;
                lockBccLockedInfoVo.setAdmitLockAmount(setting.getUnitPerAmount().multiply(new BigDecimal(lockedPortion)));
            }else {
                lockBccLockedInfoVo.setAdmitLockAmount(BigDecimal.ZERO);
            }


        }
        Coin coin = coinService.findByUnit(setting.getCoinSymbol());
        Assert.notNull(coin, "coin is not official");
        // 查询钱包
        MemberWallet memberWallet = memberWalletService.findByCoinNameAndMemberIdReadOnly(coin.getName(), memberId);
        if (null == memberWallet) {
            //没有对应的账户，新建账户
            memberWallet = memberWalletService.createMemberWallet(memberId, coin);
        }
        lockBccLockedInfoVo.setBalanceAmount(memberWallet.getBalance());

        return lockBccLockedInfoVo;
    }

    /**
     * ieo释放状态对象
     */
    @Data
    private class ReleaseResultObject {
        private Boolean result;
        private BigDecimal releasingAmount;
    }

    public LockBccAssignRecordServiceImpl getService() {
        return SpringContextUtil.getBean(LockBccAssignRecordServiceImpl.class);
    }
}
