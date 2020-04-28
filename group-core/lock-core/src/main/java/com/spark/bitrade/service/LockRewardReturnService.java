package com.spark.bitrade.service;

import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dao.LockBccAssignRecordDao;
import com.spark.bitrade.dao.LockCoinDetailDao;
import com.spark.bitrade.dao.LockIeoRestitutionIncomePlanDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;

/**
 * 锁仓推荐奖励返还service
 * @author tansitao
 * @time 2018.12.03 20:21
 */
@Service
@Slf4j
public class LockRewardReturnService {


    @Autowired
    private LockMarketRewardIncomePlanService lockMarketRewardIncomePlanService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LockBttcRestitutionIncomePlanService lockBttcRestitutionIncomePlanService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private LockIeoRestitutionIncomePlanDao lockIeoRestitutionIncomePlanDao;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private LockBccAssignRecordDao lockBccAssignRecordDao;
    @Autowired
    private LockCoinDetailDao lockCoinDetailDao;

    /**
     * 返还推荐奖励收益
     * @author tansitao
     * @time 2018/12/6 16:25 
     */
    public void returnRewardIncome(LockMarketRewardIncomePlan lockMarketRewardIncomePlan, LockCoinDetail lockCoinDetail){

        try {
            //更新佣金返还计划记录为处理中
            boolean isUpdateSuccess = lockMarketRewardIncomePlanService.updateStatus(lockMarketRewardIncomePlan.getId(), LockBackStatus.BACKING, LockBackStatus.BACK);
            if(isUpdateSuccess){
                getService().doRewardIncome(lockMarketRewardIncomePlan, lockCoinDetail);
            }else {
                log.warn("====================返回推荐奖励收益状态更新失败,返还计划ID{}，奖励明细ID{}，锁仓记录ID{}======================",
                        lockMarketRewardIncomePlan.getId(), lockMarketRewardIncomePlan.getMarketRewardDetailId(), lockMarketRewardIncomePlan.getLockDetailId());
            }
        }catch (Exception e){
            log.error("===============处理返还推荐奖励收益失败=======返还计划ID"+lockMarketRewardIncomePlan.getId()
                    + "==奖励明细ID" + lockMarketRewardIncomePlan.getMarketRewardDetailId()+"=============", e);
            lockMarketRewardIncomePlanService.updateStatus(lockMarketRewardIncomePlan.getId(), LockBackStatus.BACK, LockBackStatus.BACKING);
        }
    }

    /**
     * 处理返佣，增加用户钱包余额
     * @author tansitao
     * @time 2018/12/6 17:24 
     */
    @Transactional(rollbackFor = Exception.class)
    public void doRewardIncome(LockMarketRewardIncomePlan lockMarketRewardIncomePlan, LockCoinDetail lockCoinDetail){
        //更新成功，增加用户钱包余额
        Coin coin;
        if(lockCoinDetail == null) {
            //当无锁仓记录时，表示为CNYT增值计划运营中要求生成的返佣数据
            coin = coinService.findByUnit(lockMarketRewardIncomePlan.getSymbol());
        }else {
            coin = coinService.findByUnit(lockCoinDetail.getCoinUnit());
        }
        Assert.isTrue(coin != null, "========币种为空===========");
        //保存佣金返还资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(lockMarketRewardIncomePlan.getRewardAmount());
        memberTransaction.setMemberId(lockMarketRewardIncomePlan.getMemberId());
        memberTransaction.setType(TransactionType.LOCK_COIN_PROMOTION_AWARD_STO);
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setRefId(lockMarketRewardIncomePlan.getId()+"");
        memberTransactionService.save(memberTransaction);

        //获取钱包
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, lockMarketRewardIncomePlan.getMemberId());
        if(memberWallet == null){
            memberWallet = memberWalletService.createMemberWallet(lockMarketRewardIncomePlan.getMemberId(), coin);
        }
        //增加用户钱包余额
        memberWalletService.increaseBalance(memberWallet.getId(), lockMarketRewardIncomePlan.getRewardAmount());
        //将记录更新为已处理
        lockMarketRewardIncomePlanService.updateStatus(lockMarketRewardIncomePlan.getId(), LockBackStatus.BACKED, LockBackStatus.BACKING);
    }

    /**
      * 返还bttc锁仓分期
      * @author dengdy
      * @time 2019/4/17 15:25 
      */
    public void returnBttcRestitution(LockBttcRestitutionIncomePlan lockBttcRestitutionIncomePlan, LockCoinDetail lockCoinDetail){

        try {
            //更新佣金返还计划记录为处理中
            boolean isUpdateSuccess = lockBttcRestitutionIncomePlanService.updateStatus(lockBttcRestitutionIncomePlan.getId(), LockBackStatus.BACK, LockBackStatus.BACKING);
            if(isUpdateSuccess){
                getService().doBttcRewardRestitution(lockBttcRestitutionIncomePlan, lockCoinDetail);
            }else {
                log.warn("====================返回bttc锁仓状态更新失败,返还计划ID{}，锁仓记录ID{}======================",
                        lockBttcRestitutionIncomePlan.getId(), lockBttcRestitutionIncomePlan.getLockDetailId());
            }
        }catch (Exception e){
            log.error("===============处理返还推荐奖励收益失败=======返还计划ID"+lockBttcRestitutionIncomePlan.getId(), e);
            lockBttcRestitutionIncomePlanService.updateStatus(lockBttcRestitutionIncomePlan.getId(), LockBackStatus.BACKING, LockBackStatus.BACK);
        }
    }

    /**
      * 返还bttc锁仓分期
      * @author fatKarin
      * @time 2019/6/6 16:25 
      */
    public void returnIeoRestitution(LockIeoRestitutionIncomePlan lockIeoRestitutionIncomePlan, LockCoinDetail lockCoinDetail){

        try {
            //更新佣金返还计划记录为处理中
            int isUpdateSuccess = lockIeoRestitutionIncomePlanDao.updateStatus(lockIeoRestitutionIncomePlan.getId(), LockBackStatus.BACK, LockBackStatus.BACKING);
            if(isUpdateSuccess > 0){
                getService().doIeoRewardRestitution(lockIeoRestitutionIncomePlan, lockCoinDetail);
            }else {
                log.warn("====================返回IEO{}锁仓状态更新失败,返还计划ID{}，锁仓记录ID{}======================",
                        lockIeoRestitutionIncomePlan.getSymbol(), lockIeoRestitutionIncomePlan.getId(),
                        lockIeoRestitutionIncomePlan.getLockDetailId());
            }
        }catch (Exception e){
            log.error("===============处理返还推荐奖励收益失败=======返还计划ID"+lockIeoRestitutionIncomePlan.getId(), e);
            if("INSUFFICIENT_BALANCE".equals(e.getMessage())){
                lockIeoRestitutionIncomePlanDao.updateStatus(lockIeoRestitutionIncomePlan.getId(), LockBackStatus.BACKING, LockBackStatus.BANLANCE_INSUFFICIENT);
            }else {
                lockIeoRestitutionIncomePlanDao.updateStatus(lockIeoRestitutionIncomePlan.getId(), LockBackStatus.BACKING, LockBackStatus.BACK);
            }

        }
    }

    /**
      * 返还金钥匙锁仓金额
      * @author dengdy
      * @time 2019/5/20 15:25 
      */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void returnGoldenKeyPrincipal(LockCoinDetail lockCoinDetail){
        //更新锁仓记录为返还中
        int isUpdateSuccess = lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKING, LockStatus.LOCKED, lockCoinDetail.getId());
        Assert.isTrue(isUpdateSuccess > 0 , "更新金钥匙锁仓记录状态失败");
        //更新成功，增加用户钱包余额
        Coin coin = coinService.findByUnit(lockCoinDetail.getCoinUnit());

        Assert.isTrue(coin != null, "========币种为空===========");
        //保存佣金返还资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(lockCoinDetail.getTotalAmount());
        memberTransaction.setMemberId(lockCoinDetail.getMemberId());
        memberTransaction.setType(TransactionType.GOLD_KEY_OWN);
        memberTransaction.setComment("金钥匙锁仓返还");
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setRefId(lockCoinDetail.getId()+"");
        memberTransactionService.save(memberTransaction);

        //获取钱包
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, lockCoinDetail.getMemberId());
        if(memberWallet == null){
            memberWallet = memberWalletService.createMemberWallet(lockCoinDetail.getMemberId(), coin);
        }
        //减少锁仓余额
        MessageResult frozenResult = memberWalletService.decreaseLockBalance(memberWallet.getId(), lockCoinDetail.getTotalAmount());
        if (frozenResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //增加用户钱包余额
        MessageResult increaseResult = memberWalletService.increaseBalance(memberWallet.getId(), lockCoinDetail.getTotalAmount());

        if (increaseResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }


        //将锁仓记录更新为已返还
        int updateResult = lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKED, LockStatus.UNLOCKING, lockCoinDetail.getId());
        Assert.isTrue(updateResult > 0 , "更新金钥匙锁仓记录状态失败");
        boolean updateRemainAmountResult = lockCoinDetailService.updateRemainAmount(lockCoinDetail.getId(), BigDecimal.ZERO);
        Assert.isTrue(updateRemainAmountResult, "更新金钥匙锁仓记录剩余金额失败");

    }

    /**
      * 返还BCC赋能计划锁仓和收益
      * @author fatKarin
      * @time 2019/7/1 15:25 
      */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void returnBccEnergize(LockCoinDetail lockCoinDetail, LockCoinActivitieSetting setting){

        //更新锁仓记录为返还中
        int isUpdateSuccess = lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKING, LockStatus.LOCKED, lockCoinDetail.getId());
        Assert.isTrue(isUpdateSuccess > 0 , "更新bcc赋能锁仓记录状态失败");
        //更新成功，增加用户钱包余额
        Coin coin = coinService.findByUnit(lockCoinDetail.getCoinUnit());

        Assert.isTrue(coin != null, "========币种为空===========");
        // 计算本金加收益
        BigDecimal amount = lockCoinDetail.getTotalAmount().
                multiply(setting.getEarningRate().add(BigDecimal.ONE));

        //保存佣金返还资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(amount);
        memberTransaction.setMemberId(lockCoinDetail.getMemberId());
        memberTransaction.setType(TransactionType.ENERGIZE_LOCK);
        memberTransaction.setComment("BCC锁仓返还");
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setRefId(lockCoinDetail.getId()+"");
        memberTransactionService.save(memberTransaction);

        //获取钱包
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, lockCoinDetail.getMemberId());
        if(memberWallet == null){
            memberWallet = memberWalletService.createMemberWallet(lockCoinDetail.getMemberId(), coin);
        }
        // 减少用户冻结余额
        MessageResult frozenResult = memberWalletService.decreaseLockBalance(memberWallet.getId(), lockCoinDetail.getTotalAmount());
        if (frozenResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //增加用户钱包余额
        MessageResult increaseResult = memberWalletService.increaseBalance(memberWallet.getId(), amount);

        if (increaseResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //将锁仓记录更新为已返还
        int updateResult = lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKED, LockStatus.UNLOCKING, lockCoinDetail.getId());
        Assert.isTrue(updateResult > 0 , "更新BCC赋能锁仓记录状态失败");
        boolean updateRemainAmountResult = lockCoinDetailService.updateRemainAmount(lockCoinDetail.getId(), BigDecimal.ZERO);
        Assert.isTrue(updateRemainAmountResult, "更新BCC赋能锁仓记录剩余金额失败");

        // 清除份数
        //add by qhliao BCC释放奖励金bug修复 查询出剩余的锁仓金额
        //Integer portion = lockCoinDetail.getTotalAmount().divideToIntegralValue(setting.getUnitPerAmount()).intValue() * 2;
        Integer portion=0;
        BigDecimal remainLockAmount = lockCoinDetailDao.findRemainLockAmount(lockCoinDetail.getMemberId(),
                lockCoinDetail.getType().getOrdinal(), LockStatus.LOCKED.getOrdinal());
        remainLockAmount=remainLockAmount==null?BigDecimal.ZERO:remainLockAmount;
        LockBccAssignRecord record = lockBccAssignRecordDao.findByMemberId(lockCoinDetail.getMemberId());
        //需要释放的奖励金
        BigDecimal releaseAmount = record.getReleaseAmount().subtract(remainLockAmount.multiply(new BigDecimal(2)));
        if(releaseAmount.compareTo(BigDecimal.ZERO)>=0){
            BigDecimal divide = releaseAmount.divide(setting.getUnitPerAmount());
            portion=divide.intValue();
        }
        // 更新用户佣金记录
        int result = lockBccAssignRecordDao.mounthClearUpdateByMemberId(lockCoinDetail.getMemberId(), portion, setting.getUnitPerAmount());

        Assert.isTrue(result > 0, "更新用户佣金记录失败");
    }

    /**
      * 处理bttc分期返还，增加用户钱包余额
      * @author dengdy
      * @time 2019/4/17 15:44 
      */
    @Transactional(rollbackFor = Exception.class)
    public void doBttcRewardRestitution(LockBttcRestitutionIncomePlan lockBttcRestitutionIncomePlan, LockCoinDetail lockCoinDetail) throws Exception{
        //更新成功，增加用户钱包余额
        Coin coin = coinService.findByUnit(lockCoinDetail.getCoinUnit());

        Assert.isTrue(coin != null, "========币种为空===========");
        //保存佣金返还资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(lockBttcRestitutionIncomePlan.getRestitutionAmount());
        memberTransaction.setMemberId(lockBttcRestitutionIncomePlan.getMemberId());
        memberTransaction.setType(TransactionType.IEO_ACTIVITY);
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setRefId(lockBttcRestitutionIncomePlan.getId()+"");
        memberTransactionService.save(memberTransaction);

        //判断是否是固定账户的名单里会员

        String innerList = lockBttcRestitutionIncomePlanService.findInnerList();
        if(innerList.contains(","+lockBttcRestitutionIncomePlan.getMemberId()+",")){
            //如果是
            log.info("特殊用户----------------------");
            MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, lockBttcRestitutionIncomePlan.getMemberId());
            if(memberWallet == null){
                memberWallet = memberWalletService.createMemberWallet(lockBttcRestitutionIncomePlan.getMemberId(), coin);
            }
            memberWalletService.decreaseLockBalance(memberWallet.getId(),lockBttcRestitutionIncomePlan.getRestitutionAmount());
            //增加指定用户钱包余额 会员id:387359
            MemberWallet memberWallet2 = memberWalletService.findByCoinAndMemberId(coin,387359L);
            memberWalletService.increaseBalance(memberWallet2.getId(), lockBttcRestitutionIncomePlan.getRestitutionAmount());
            MemberTransaction memberTransaction2 = new MemberTransaction();
            memberTransaction2.setAmount(BigDecimal.ZERO.subtract(lockBttcRestitutionIncomePlan.getRestitutionAmount()));
            memberTransaction2.setMemberId(lockBttcRestitutionIncomePlan.getMemberId());
            memberTransaction2.setType(TransactionType.TRANSFER_ACCOUNTS);
            memberTransaction2.setSymbol(coin.getUnit());
            memberTransaction2.setRefId(lockBttcRestitutionIncomePlan.getId()+"");
            memberTransactionService.save(memberTransaction2);
            MemberTransaction memberTransaction3 = new MemberTransaction();
            memberTransaction3.setAmount(lockBttcRestitutionIncomePlan.getRestitutionAmount());
            memberTransaction3.setMemberId(387359L);
            memberTransaction3.setType(TransactionType.TRANSFER_ACCOUNTS);
            memberTransaction3.setSymbol(coin.getUnit());
            memberTransaction3.setRefId(lockBttcRestitutionIncomePlan.getId()+"");
            memberTransactionService.save(memberTransaction3);
        }else {
        //如果不是
        //获取钱包
            log.info("普通用户----------------------");
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, lockBttcRestitutionIncomePlan.getMemberId());
        if(memberWallet == null){
            memberWallet = memberWalletService.createMemberWallet(lockBttcRestitutionIncomePlan.getMemberId(), coin);
        }
        //增加用户钱包余额
        memberWalletService.decreaseLockBalance(memberWallet.getId(),lockBttcRestitutionIncomePlan.getRestitutionAmount());
        memberWalletService.increaseBalance(memberWallet.getId(), lockBttcRestitutionIncomePlan.getRestitutionAmount());
        }


        //将记录更新为已处理
        boolean b = lockBttcRestitutionIncomePlanService.updateStatus(lockBttcRestitutionIncomePlan.getId(), LockBackStatus.BACKING, LockBackStatus.BACKED);
        if(!b){
            throw new Exception("更新记录时失败");
        }
        lockCoinDetailDao.updateRemainAmount(lockCoinDetail.getId(),
                lockCoinDetail.getRemainAmount().subtract(lockBttcRestitutionIncomePlan.getRestitutionAmount()).compareTo(BigDecimal.ZERO)<0?BigDecimal.ZERO:
                        lockCoinDetail.getRemainAmount().subtract(lockBttcRestitutionIncomePlan.getRestitutionAmount()));
        if(lockBttcRestitutionIncomePlan.getPeriod()==5){
            lockCoinDetailDao.updateLockCoinStatus(lockCoinDetail.getId(),LockStatus.UNLOCKED,LockStatus.LOCKED);
        }
    }

    /**
      * 处理bttc分期返还，增加用户钱包余额
      * @author fatKarin
      * @time 2019/6/6 16:25 
      */
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void doIeoRewardRestitution(LockIeoRestitutionIncomePlan lockIeoRestitutionIncomePlan, LockCoinDetail lockCoinDetail){
        //更新成功，增加用户钱包余额
        Coin coin = coinService.findByUnit(lockCoinDetail.getCoinUnit());

        Assert.isTrue(coin != null, "========币种为空===========");
        //保存佣金返还资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(lockIeoRestitutionIncomePlan.getRestitutionAmount());
        memberTransaction.setMemberId(lockIeoRestitutionIncomePlan.getMemberId());
        memberTransaction.setType(TransactionType.IEO_ACTIVITY);
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setRefId(lockIeoRestitutionIncomePlan.getId()+"");
        memberTransaction.setComment("IEO计划返佣");
        memberTransactionService.save(memberTransaction);

        //获取钱包
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, lockIeoRestitutionIncomePlan.getMemberId());
        if(memberWallet == null){
            memberWallet = memberWalletService.createMemberWallet(lockIeoRestitutionIncomePlan.getMemberId(), coin);
        }
        //减少锁仓余额
        MessageResult frozenResult = memberWalletService.decreaseLockBalance(memberWallet.getId(), lockIeoRestitutionIncomePlan.getRestitutionAmount());
        if (frozenResult.getCode() != 0) {
            throw new IllegalArgumentException("INSUFFICIENT_BALANCE");
        }

        //增加用户钱包余额
        MessageResult increaseResult = memberWalletService.increaseBalance(memberWallet.getId(), lockIeoRestitutionIncomePlan.getRestitutionAmount());

        if (increaseResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //将记录更新为已处理
        int updateResult = lockIeoRestitutionIncomePlanDao.updateStatus(lockIeoRestitutionIncomePlan.getId(), LockBackStatus.BACKING, LockBackStatus.BACKED);
        Assert.isTrue(updateResult > 0 , "更新ieo 返还记录状态失败");
    }

    public LockRewardReturnService getService(){
        return SpringContextUtil.getBean(LockRewardReturnService.class);
    }
}
