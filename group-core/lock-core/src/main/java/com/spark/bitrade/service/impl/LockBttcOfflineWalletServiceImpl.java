package com.spark.bitrade.service.impl;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.LockBttcIeoOfflineWalletDao;
import com.spark.bitrade.dao.LockBttcOfflineWalletDao;
import com.spark.bitrade.dao.LockCoinDetailDao;
import com.spark.bitrade.dao.MemberTransactionDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.LockBttcOfflineWalletMapper;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PriceUtil;
import com.spark.bitrade.vo.LockBttcOfflineWalletVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Service
@Slf4j
public class LockBttcOfflineWalletServiceImpl implements ILockBttcOfflineWalletService {

    @Autowired
    private LockBttcOfflineWalletDao lockBttcOfflineWalletDao;

    @Autowired
    private LockBttcOfflineWalletMapper lockBttcOfflineWalletMapper;

    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LockCoinDetailDao lockCoinDetailDao;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberTransactionDao memberTransactionDao;
    @Autowired
    private CoinService coinService;
    @Autowired
    private LockBttcIeoOfflineWalletDao lockBttcIeoOfflineWalletDao;

    @Override
    public LockBttcOfflineWalletVo findLockBttcOfflineWalletVoByMemberId(Long memberId) {
        return lockBttcOfflineWalletMapper.findLockBttcOfflineWalletVoByMemberId(memberId);
    }

    @Override
    public   BigDecimal findLockBttcIeoOfflineWalletBalanceByMemberId(Long memberId){
        return lockBttcOfflineWalletMapper. findLockBttcIeoOfflineWalletBalanceByMemberId( memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void lockReleaseGoldenKey(Long memberId, BigDecimal lockAmount, Long activityId) throws Exception{
        // 查询 金钥匙活动，以获取最小锁仓金额
        log.info("查询金钥匙活动");
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(activityId);
        Assert.isTrue(lockCoinActivitieSetting != null, "活动不存在");
        Assert.isTrue(lockCoinActivitieSetting.getStatus() == LockSettingStatus.VALID, "活动" + lockCoinActivitieSetting.getStatus().getCnName());
        Assert.isTrue(lockCoinActivitieSetting.getEndTime().after(new Date()), "活动已过期");
        Assert.isTrue(lockAmount.compareTo(lockCoinActivitieSetting.getMinBuyAmount()) >= 0
                , "锁仓个数应不小于最小锁仓个数");
        log.info("查询会员钱包");
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol(), memberId);
        Assert.isTrue(memberWallet != null, "用户BTTC钱包不存在");
        Assert.isTrue(memberWallet.getBalance().compareTo(BigDecimal.ZERO) > 0, "可用余额应大于0");
        // 冻结锁定金额
        log.info("冻结会员锁仓金额");
        MessageResult freezeReulst = memberWalletService.freezeBalanceToLockBalance(memberWallet, lockAmount);
        if (freezeReulst.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        // 增加锁仓记录
        log.info("增加锁仓记录");
        Long lockCoinDetailId = this.generateLockRecord(memberId, lockCoinActivitieSetting, lockAmount);

        LockBttcOfflineWallet lockBttcOfflineWallet = lockBttcOfflineWalletDao.findByMemberId(memberId);


        // 应该释放金钥匙数量
        BigDecimal releaseGoldenKeyAmount = lockAmount.divide(lockCoinActivitieSetting.getMinBuyAmount()
                ,8, BigDecimal.ROUND_DOWN);

        Member member = memberService.findOne(memberId);

        // 本人锁仓，释放金钥匙
        changeUserAccount(member, releaseGoldenKeyAmount, lockBttcOfflineWallet, lockCoinActivitieSetting, TransactionType.GOLD_KEY_OWN,lockCoinDetailId);

        Member lastLevelMember = null;

        // 团队循环解仓
        do {
            if(member.getInviterId() != null) {
                lastLevelMember = memberService.findOne(member.getInviterId());
                if (lastLevelMember != null) {
                    releaseGoldenKeyAmount = releaseGoldenKeyAmount.divide(new BigDecimal("10"), 8, BigDecimal.ROUND_DOWN);
                    if(releaseGoldenKeyAmount.compareTo(BigDecimal.ZERO) <= 0) { // 释放金钥匙数小于等于0，则不再查询上级
                        break;
                    }
                    LockBttcOfflineWallet lastLevelMemberOfflineWallet = lockBttcOfflineWalletDao.findByMemberId(lastLevelMember.getId());
                    changeUserAccount(lastLevelMember, releaseGoldenKeyAmount, lastLevelMemberOfflineWallet, lockCoinActivitieSetting, TransactionType.GOLD_KEY_TEAM, lockCoinDetailId);
                    member = lastLevelMember;
                }
            }
        } while (lastLevelMember != null && lastLevelMember.getInviterId() != null);

    }

    /**
     * 资产表账户余额减少
     * 星客总账户锁定余额解冻，释放
     * 星客账户余额增加
     * @param member
     * @param releaseAmout
     * @param lockBttcOfflineWallet
     * @param lockCoinActivitieSetting
     * @param transactionType
     */
    private void changeUserAccount(Member member, BigDecimal releaseAmout, LockBttcOfflineWallet lockBttcOfflineWallet,
                                   LockCoinActivitieSetting lockCoinActivitieSetting, TransactionType transactionType,Long lockCoinDetailId) {
        // 用户金钥匙账户不存在，直接返回
        if(lockBttcOfflineWallet == null ) {
            log.info("用户" + member.getId() + "金钥匙账户不存在");
            return;
        }

        if(lockBttcOfflineWallet.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            log.info("用户" + member.getId() + "金钥匙账户金额为0");
            return;
        }

        log.info("更新金钥匙账户" + member.getId() +"金钥匙数量");
        // 更新金钥匙账户，减少金钥匙金额，并将减少数量 记录到上次更新数量字段中
        long updateOfflineWalletResult = lockBttcOfflineWalletDao
                .decreaseOfflineWallet(lockBttcOfflineWallet.getId(), releaseAmout);
        Assert.isTrue(updateOfflineWalletResult > 0, "释放金钥匙失败");

        // 会员钱包
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol(), member.getId());

        lockBttcOfflineWallet = lockBttcOfflineWalletDao.findByMemberId(member.getId());
        // 实际释放金钥匙数量
        BigDecimal releaseGoldenKeyAmountReality = lockBttcOfflineWallet.getLastReleaseAmount();
        log.info("会员" + member.getId() + "金钥匙账户释放金钥匙" + releaseGoldenKeyAmountReality + "个");

        // 总账户会员信息
        Member silkMember = memberService.findByPhone("17358331831");
        // 总额账户钱包
        MemberWallet totalAmountWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol(), silkMember.getId());

        log.info("金钥匙总账户开始转出金钥匙到会员" + member.getId() + "账户");
        // 解冻，减少总账户金钥匙数量
        MessageResult decreaseReulst = memberWalletService.subtractFreezeBalance(totalAmountWallet, releaseGoldenKeyAmountReality);
        if (decreaseReulst.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        log.info("金钥匙总账户转出" + releaseGoldenKeyAmountReality + "个金钥匙到会员" + member.getId() + "账户");
        // 新增转账交易记录
        MemberTransaction totalTransaction = new MemberTransaction();
        totalTransaction.setMemberId(silkMember.getId());
        totalTransaction.setAmount(BigDecimal.ZERO.subtract(releaseGoldenKeyAmountReality));
        totalTransaction.setType(TransactionType.TRANSFER_ACCOUNTS);
        totalTransaction.setSymbol(lockCoinActivitieSetting.getCoinSymbol());
        memberTransactionService.save(totalTransaction);



        // 星客账户增加金钥匙数量
        MessageResult increaseReulst = memberWalletService.increaseBalance(memberWallet.getId(), releaseGoldenKeyAmountReality);
        if (increaseReulst.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        log.info("金钥匙总账户转出" + releaseGoldenKeyAmountReality + "个金钥匙到会员" + member.getId() + "账户");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        // 新增交易记录 以储存释放金钥匙金额，保持事务一致性，交易记录作为星客账户金钥匙增加记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setMemberId(member.getId());
        memberTransaction.setAmount(releaseGoldenKeyAmountReality);
        memberTransaction.setType(transactionType);
        memberTransaction.setSymbol(lockCoinActivitieSetting.getCoinSymbol());
        memberTransaction.setRefId(String.valueOf(lockCoinDetailId));
        memberTransactionService.save(memberTransaction);

        memberTransactionDao.updateCreateTime(calendar.getTime(), memberTransaction.getId());
    }

    /**
     * 金钥匙锁仓产生锁仓记录
     * @param memberId
     * @param setting
     * @param amount
     */
    private Long generateLockRecord(Long memberId, LockCoinActivitieSetting setting, BigDecimal amount) {
        PriceUtil priceUtil = new PriceUtil();
        //获取锁仓币种人民币价格
        BigDecimal coinCnyPrice = priceUtil.getCoinCnyPrice(restTemplate, setting.getCoinSymbol());
        //获取锁仓币种USDT价格
        BigDecimal coinUSDTPrice = priceUtil.getCoinCnyPrice(restTemplate, setting.getCoinSymbol());
        //获取USDT的人民币价格
        BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);

        // 开始解锁时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, setting.getLockDays());

        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        lockCoinDetail.setMemberId(memberId);
        lockCoinDetail.setType(LockType.GOLD_KEY);
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
        lockCoinDetail.setRemark(String.format("金钥匙锁仓：%f", amount));
        lockCoinDetail.setLockDays(setting.getLockDays());
        lockCoinDetail.setEarningRate(BigDecimal.ZERO);
        lockCoinDetail.setUnitPerAmount(BigDecimal.ONE);
        lockCoinDetail.setLockRewardSatus(LockRewardSatus.DEFAULT_REWARD);
        lockCoinDetail.setSmsSendStatus(SmsSendStatus.NO_SMS_SEND);
        lockCoinDetail.setLockCycle(setting.getLockCycle());
        lockCoinDetail.setBeginDays(setting.getLockDays());
        lockCoinDetail.setCycleDays(setting.getCycleDays());

        lockCoinDetailDao.save(lockCoinDetail);

        // 保存BTTC扣除资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setMemberId(memberId);
        memberTransaction.setAmount(BigDecimal.ZERO.subtract(amount));
        memberTransaction.setType(TransactionType.GOLD_KEY_OWN);
        memberTransaction.setSymbol(setting.getCoinSymbol());
        memberTransaction.setRefId(String.valueOf(lockCoinDetail.getId()));
        memberTransactionService.save(memberTransaction);

        return lockCoinDetail.getId();
    }


    /**
     *  huyu
     * @param memberId
     * @param lockAmount   2000
     * @param activityId    detailId
     * @param changeAmount  18000
     * @param unlockRate   20%
     * @param flag        扣除金钥匙账户还是扣除二次锁仓剩余数
     * @param reduceAmount   2000(取上次结余)
     * @param doRecord   是否生成交易记录
     * @return
     * @throws Exception
     */
    @Override
    public Long lockReleaseGoldenKeyForIeo(Long memberId, BigDecimal lockAmount, Long activityId,
                 BigDecimal changeAmount,BigDecimal unlockRate,boolean flag , BigDecimal reduceAmount,
                                           boolean doRecord,BigDecimal lastAmount) throws Exception{

        LockBttcIeoOfflineWallet lockBttcOfflineWallet = lockBttcIeoOfflineWalletDao.findByMemberId(memberId);
        if(lockBttcOfflineWallet==null){
            MemberWallet memberWallet1 = memberWalletService.findByCoinUnitAndMemberId("SLU", memberId);
//            Assert.isTrue(memberWallet1 != null, "用户钱包地址不存在");
            lockBttcOfflineWallet = new LockBttcIeoOfflineWallet();
            lockBttcOfflineWallet.setAddress(memberWallet1==null?"":memberWallet1.getAddress());
            lockBttcOfflineWallet.setBalance(changeAmount);//18000
            lockBttcOfflineWallet.setMemberId(memberId);
            lockBttcOfflineWallet.setCoinId("BTTC");
            lockBttcOfflineWallet.setUnlockedAmount(BigDecimal.ZERO);
            lockBttcIeoOfflineWalletDao.save(lockBttcOfflineWallet);
            // 新增交易记录 以储存释放金钥匙金额，保持事务一致性，交易记录作为星客账户金钥匙增加记录
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setMemberId(memberId);
                memberTransaction.setAmount(changeAmount);
                memberTransaction.setType(TransactionType.GOLD_KEY_OWN);
                memberTransaction.setSymbol("BTTC");
                memberTransaction.setRefId(activityId+"");
            memberTransaction.setComment("IEO-BTTC金钥匙账户");
                memberTransactionService.save(memberTransaction);

                memberTransactionDao.updateCreateTime(new Date(), memberTransaction.getId());
        }else {
            if(changeAmount.compareTo(BigDecimal.ZERO)>0){
                lockBttcIeoOfflineWalletDao.addBalance(lockBttcOfflineWallet.getId(),changeAmount);
                // 新增交易记录 以储存释放金钥匙金额，保持事务一致性，交易记录作为星客账户金钥匙增加记录
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setMemberId(memberId);
                    memberTransaction.setAmount(changeAmount);
                    memberTransaction.setType(TransactionType.GOLD_KEY_OWN);
                    memberTransaction.setSymbol("BTTC");
                    memberTransaction.setRefId(activityId+"");
                    memberTransaction.setComment("IEO-BTTC金钥匙账户");
                    memberTransactionService.save(memberTransaction);

                    memberTransactionDao.updateCreateTime(new Date(), memberTransaction.getId());
            }
        }
        // 增加锁仓记录
        log.info("增加锁仓记录");
        Long lockCoinDetailId = this.generateLockRecordWithLeft(memberId, null, lockAmount,lastAmount.subtract(lockAmount.multiply(unlockRate)),doRecord);//+2000

        Member member = memberService.findOne(memberId);
        Assert.isTrue(member != null, "用户不存在");
        // 本人锁仓，释放金钥匙
        if(flag){
            changeUserAccount3(member, reduceAmount, TransactionType.GOLD_KEY_OWN,lockCoinDetailId);
        }else {
            changeUserAccount2(member, lockAmount.multiply(unlockRate), lockBttcOfflineWallet, TransactionType.GOLD_KEY_OWN,lockCoinDetailId);
        }
        return lockCoinDetailId;
    }



    /**(IEO解仓用)
     * 资产表账户余额减少
     * 星客总账户锁定余额解冻，释放
     * 星客账户余额增加
     * @param member
     * @param releaseAmout
     * @param lockBttcOfflineWallet
     * @param transactionType
     */
    private void changeUserAccount2(Member member, BigDecimal releaseAmout, LockBttcIeoOfflineWallet lockBttcOfflineWallet,
                                    TransactionType transactionType,Long lockCoinDetailId) {
        // 用户金钥匙账户不存在，直接返回
//        if(lockBttcOfflineWallet == null ) {
//            log.info("用户" + member.getId() + "金钥匙账户不存在");
//            return;
//        }

//        if(lockBttcOfflineWallet.getBalance().compareTo(BigDecimal.ZERO) == 0) {
//            log.info("用户" + member.getId() + "金钥匙账户金额为0");
//            return;
//        }

        log.info("更新金钥匙账户" + member.getId() +"金钥匙数量");
        // 更新金钥匙账户，减少金钥匙金额，并将减少数量 记录到上次更新数量字段中
        long updateOfflineWalletResult = lockBttcIeoOfflineWalletDao
                .decreaseOfflineWallet(lockBttcOfflineWallet.getId(), releaseAmout);
        Assert.isTrue(updateOfflineWalletResult > 0, "释放金钥匙失败");
        // 新增交易记录 以储存释放金钥匙金额，保持事务一致性，交易记录作为星客账户金钥匙增加记录
        MemberTransaction memberTransaction2 = new MemberTransaction();
        memberTransaction2.setMemberId(member.getId());
        memberTransaction2.setAmount(BigDecimal.ZERO.subtract(releaseAmout));
        memberTransaction2.setType(TransactionType.GOLD_KEY_OWN);
        memberTransaction2.setSymbol("BTTC");
        memberTransaction2.setRefId(lockCoinDetailId+"");
        memberTransaction2.setComment("IEO-BTTC金钥匙账户");
        memberTransactionService.save(memberTransaction2);
        log.info("查询会员钱包");
        // 会员钱包
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId("BTTC", member.getId());

        if(memberWallet==null){
            memberWallet = memberWalletService.createMemberWallet(member.getId(),coinService.findByUnit("BTTC"));
        }

//        lockBttcOfflineWallet = lockBttcIeoOfflineWalletDao.findByMemberId(member.getId());
        // 实际释放金钥匙数量
//        BigDecimal releaseGoldenKeyAmountReality = lockBttcOfflineWallet.getLastReleaseAmount();
//        log.info("会员" + member.getId() + "金钥匙账户释放金钥匙" + releaseGoldenKeyAmountReality + "个");


        // 星客账户增加金钥匙数量
        MessageResult increaseReulst = memberWalletService.increaseBalance(memberWallet.getId(), releaseAmout);
        memberWalletService.decreaseLockBalance(memberWallet.getId(), releaseAmout);
        if (increaseReulst.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        log.info("金钥匙总账户转出" + releaseAmout + "个金钥匙到会员" + member.getId() + "账户");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        // 新增交易记录 以储存释放金钥匙金额，保持事务一致性，交易记录作为星客账户金钥匙增加记录
        if(releaseAmout.compareTo(BigDecimal.ZERO)>0){
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setMemberId(member.getId());
            memberTransaction.setAmount(releaseAmout);
            memberTransaction.setType(transactionType);
            memberTransaction.setSymbol("BTTC");
            memberTransaction.setRefId(String.valueOf(lockCoinDetailId));
            memberTransactionService.save(memberTransaction);

            memberTransactionDao.updateCreateTime(calendar.getTime(), memberTransaction.getId());
        }

    }



    /**(IEO解仓用)
     * 资产表账户余额减少
     * 星客总账户锁定余额解冻，释放
     * 星客账户余额增加
     * @param member
     * @param releaseAmout
     * @param transactionType
     */
    private void changeUserAccount3(Member member, BigDecimal releaseAmout,
                                    TransactionType transactionType,Long lockCoinDetailId) {

        // 会员钱包
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId("BTTC", member.getId());

        if(memberWallet==null){
            memberWallet = memberWalletService.createMemberWallet(member.getId(),coinService.findByUnit("BTTC"));
        }

        // 星客账户增加金钥匙数量
        MessageResult increaseReulst = memberWalletService.increaseBalance(memberWallet.getId(), releaseAmout);
        memberWalletService.decreaseLockBalance(memberWallet.getId(), releaseAmout);
        if (increaseReulst.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        log.info("金钥匙总账户转出" + releaseAmout + "个金钥匙到会员" + member.getId() + "账户");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        // 新增交易记录 以储存释放金钥匙金额，保持事务一致性，交易记录作为星客账户金钥匙增加记录
        if(releaseAmout.compareTo(BigDecimal.ZERO)>0){
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setMemberId(member.getId());
            memberTransaction.setAmount(releaseAmout);
            memberTransaction.setType(transactionType);
            memberTransaction.setSymbol("BTTC");
            memberTransaction.setRefId(String.valueOf(lockCoinDetailId));
            memberTransactionService.save(memberTransaction);
            memberTransactionDao.updateCreateTime(calendar.getTime(), memberTransaction.getId());
        }



    }


    /**
     * 金钥匙锁仓产生锁仓记录
     * @param memberId
     * @param setting
     * @param amount
     */
    private Long generateLockRecordWithLeft(Long memberId, LockCoinActivitieSetting setting, BigDecimal amount,BigDecimal left,boolean doRecord) {
        if(amount.compareTo(new BigDecimal("0.0001"))<=0){
            return null;
        }

        // 开始解锁时间
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 30);
        Date circleDate = cal.getTime();

        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        lockCoinDetail.setMemberId(memberId);
        lockCoinDetail.setType(LockType.GOLD_KEY);
        lockCoinDetail.setCoinUnit("BTTC");
        lockCoinDetail.setRefActivitieId(39L);
        lockCoinDetail.setTotalAmount(amount);
        lockCoinDetail.setLockPrice(BigDecimal.ZERO);
        lockCoinDetail.setRemainAmount(amount);
        lockCoinDetail.setLockTime(new Date());
        lockCoinDetail.setPlanUnlockTime(circleDate);
        lockCoinDetail.setPlanIncome(BigDecimal.ZERO);
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setUnlockTime(circleDate);
        lockCoinDetail.setUsdtPriceCNY(BigDecimal.ZERO);
        lockCoinDetail.setTotalCNY(left.compareTo(BigDecimal.ZERO)<=0?BigDecimal.ZERO:left);
        lockCoinDetail.setRemark(String.format("金钥匙锁仓：%f", amount));
        lockCoinDetail.setLockDays(30);
        lockCoinDetail.setEarningRate(BigDecimal.ZERO);
        lockCoinDetail.setUnitPerAmount(BigDecimal.ONE);
        lockCoinDetail.setLockRewardSatus(LockRewardSatus.DEFAULT_REWARD);
        lockCoinDetail.setSmsSendStatus(SmsSendStatus.NO_SMS_SEND);
        lockCoinDetail.setLockCycle(30);
        lockCoinDetail.setBeginDays(1);
        lockCoinDetail.setCycleDays(30);

        lockCoinDetailDao.save(lockCoinDetail);
//        if(doRecord){
//            // 保存BTTC扣除资金记录
//            MemberTransaction memberTransaction = new MemberTransaction();
//            memberTransaction.setMemberId(memberId);
//            memberTransaction.setAmount(BigDecimal.ZERO.subtract(amount));
//            memberTransaction.setType(TransactionType.GOLD_KEY_OWN);
//            memberTransaction.setSymbol("BTTC");
//            memberTransaction.setRefId(String.valueOf(lockCoinDetail.getId()));
//            memberTransactionService.save(memberTransaction);
//        }


        return lockCoinDetail.getId();
    }






}
