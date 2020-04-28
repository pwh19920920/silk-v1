package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.LockUttMemberDao;
import com.spark.bitrade.dao.LockUttReleasePlanDao;
import com.spark.bitrade.dto.LockUttDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.mapper.dao.LockUttMemberMapper;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.AssertUtil;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.LockBttcImportVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 导入用户表 服务实现类
 * </p>
 *
 * @author qiliao
 * @since 2019-08-15
 */
@Service
@Slf4j
public class LockUttMemberServiceImpl extends ServiceImpl<LockUttMemberMapper, LockUttMember> implements LockUttMemberService {

    private static final String UTT = "UTT";
    @Autowired
    private LockUttMemberDao lockUttMemberDao;

    @Autowired
    private LockUttReleasePlanDao lockUttReleasePlanDao;
    @Autowired
    private MemberWalletService jpaMemberWalletService;

    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    /**
     * 锁仓utt
     *
     * @param
     * @param lockUttDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void lockUtt(LockUttMember uttMember, LockUttDto lockUttDto) {
        generateTransaction(uttMember.getMemberId(), lockUttDto.getTotalMemberId(), uttMember.getId(), uttMember.getAmount());
        //生成lock coin detail
        LockCoinDetail detail = generateLockDetail(uttMember,lockUttDto);
        //生成返还计划 释放计划 第一天返回30% 后续每天0.7%
        generateReleasePlan(uttMember,lockUttDto,detail);
        uttMember.setStatus(UttMemberStatus.PROCESSED);
        uttMember.setRemark("处理成功,锁仓成功!");
        uttMember.setUpdateTime(new Date());
        lockUttMemberDao.save(uttMember);
    }

    /**
     * 生成释放计划
     *
     * @param uttMember
     * @param lockUttDto
     */
    private void generateReleasePlan(LockUttMember uttMember, LockUttDto lockUttDto, LockCoinDetail detail) {
        List<LockUttReleasePlan> plans = new ArrayList<>();
        //第一期
        LockUttReleasePlan plan;

        BigDecimal amount = uttMember.getAmount();
        //释放天数 计算公式 总金额*(1-第一期释放比例) 除 总金额*后面释放比例
        int releasedDays =
                amount.multiply(new BigDecimal(1).subtract(lockUttDto.getFirstRate()))
                        .divide(amount.multiply(lockUttDto.getAvgRate())).intValue()+1;
        //生成101期
        for (int i = 1; i <= releasedDays; i++) {
            plan = new LockUttReleasePlan();
            plan.setMemberId(uttMember.getMemberId());
            plan.setLockDetailId(detail.getId());
            plan.setCreateTime(new Date());
            Date unlockTime = detail.getUnlockTime();
            Calendar ca = Calendar.getInstance();
            ca.setTime(unlockTime);
            ca.add(Calendar.DAY_OF_YEAR, i - 1);
            plan.setPlanUnlockTime(ca.getTime());
            plan.setCoinUnit(UTT);
            if (i == 1) {
                plan.setUnlockAmount(amount.multiply(lockUttDto.getFirstRate()));
            } else {
                plan.setUnlockAmount(amount.multiply(lockUttDto.getAvgRate()));
            }
            plan.setStatus(UttReleaseStatus.BE_RELEASING);
            plan.setRemark("UTT释放计划第" + i + "期");

            plans.add(plan);
        }
        lockUttReleasePlanDao.save(plans);
    }


    /**
     * 生成锁仓记录并返回
     *
     * @return
     */
    private LockCoinDetail generateLockDetail(LockUttMember uttMember,LockUttDto lockUttDto) {
        LockCoinDetail detail = new LockCoinDetail();
        detail.setMemberId(uttMember.getMemberId());
        detail.setType(LockType.LOCK_UTT);
        detail.setCoinUnit(UTT);
        detail.setTotalAmount(uttMember.getAmount());
        detail.setRemainAmount(uttMember.getAmount());
        //默认8月15日
        Date unlockTime = DateUtil.stringToDate("2019-09-14 14:30:00", "yyyy-MM-dd HH:mm:ss");
        detail.setLockTime(new Date());
        detail.setPlanUnlockTime(unlockTime);
        detail.setStatus(LockStatus.LOCKED);
        detail.setUnlockTime(unlockTime);
        detail.setRemark("UTT锁仓");
        detail.setLockDays(30);
        detail.setUsdtPriceCNY(lockUttDto.getUsdtPrice());
        detail.setLockPrice(lockUttDto.getCoinUSDTPrice());
        detail.setTotalCNY(lockUttDto.getCoinCnyPrice().multiply(uttMember.getAmount()));
        LockCoinDetail save = lockCoinDetailService.save(detail);
        return save;
    }


    /**
     * 生成一系列交易流水 和钱包转账记录
     *
     * @param memberId
     * @param totalMemberId
     * @param uttId
     * @param amount
     */
    private void generateTransaction(Long memberId, Long totalMemberId, Long uttId, BigDecimal amount) {

        MemberWallet memberWallet = jpaMemberWalletService.findByCoinUnitAndMemberId(UTT, memberId);
        if (memberWallet == null) {
            Coin coin = coinService.findByUnit(UTT);
            AssertUtil.notNull(coin, MessageCode.INVALID_OTC_COIN);
            //若钱包不存在则创建钱包 新开启事务创建钱包 后续出错不回滚
            memberWallet = jpaMemberWalletService.createMemberWallet(memberId, coin);
        }

        //查询总账户钱包
        MemberWallet totalWallet = jpaMemberWalletService.findByCoinUnitAndMemberId(UTT, totalMemberId);
        Assert.notNull(totalWallet, "总账户钱包不存在");

        //扣除总帐户余额
        MessageResult messageResult = jpaMemberWalletService.subtractBalance(totalWallet, amount);
        AssertUtil.isTrue(messageResult.isSuccess(), MessageCode.FAILED_SUBTRACT_BALANCE);
        //生成总帐户流水
        MemberTransaction totalTransaction =
                creteTransaction(totalMemberId, BigDecimal.ZERO.subtract(amount), "总账户UTT转出", uttId, memberWallet.getAddress(),TransactionType.LOCK_UTT);
        transactionService.save(totalTransaction);

        //用户锁仓余额增加 流水记录
        MessageResult lockBlanceRes = jpaMemberWalletService.increaseLockBalance(memberWallet.getId(), amount);
        AssertUtil.isTrue(lockBlanceRes.isSuccess(), MessageCode.FAILED_ADD_LOCK_BLANCE);
        MemberTransaction memberTransaction =
                creteTransaction(memberId, amount, "总账户UTT转出到用户锁仓余额", uttId, memberWallet.getAddress(),TransactionType.LOCK_UTT);
        transactionService.save(memberTransaction);


    }


    /**
     * 创建流水实体
     *
     * @return
     */
    @Override
    public MemberTransaction creteTransaction(Long memberId,
                                               BigDecimal amount,
                                               String comment,
                                               Long uttId,
                                               String address,
                                               TransactionType transactionType) {
        MemberTransaction transaction = new MemberTransaction();
        transaction.setMemberId(memberId);
        transaction.setAmount(amount);
        transaction.setCreateTime(new Date());
        transaction.setType(transactionType);
        transaction.setSymbol(UTT);
        transaction.setAddress(address);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setRefId(String.valueOf(uttId));
        transaction.setComment(comment);

        return transaction;
    }

    @Override
    public MemberTransaction creteTransactionNew(Long memberId, BigDecimal amount, String comment,
                                                 Long uttId, String address, TransactionType transactionType, String coin) {
        MemberTransaction transaction = new MemberTransaction();
        transaction.setMemberId(memberId);
        transaction.setAmount(amount);
        transaction.setCreateTime(new Date());
        transaction.setType(transactionType);
        transaction.setSymbol(coin);
        transaction.setAddress(address);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setRefId(String.valueOf(uttId));
        transaction.setComment(comment);

        return transaction;
    }

    @Override
    public List<LockBttcImportVo> findBttcImportList(Long memberId, String tableName) {
        List<LockBttcImportVo> bttcImportList = baseMapper.findBttcImportList(memberId, tableName);
        return bttcImportList;
    }

    @Override
    public List<String> findAllImportTable() {
        return baseMapper.findAllImportTable();
    }

    @Override
    public String tableExist(String tableName){
        return baseMapper.tableExist(tableName);
    }
}














