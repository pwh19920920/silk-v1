package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.constant.HqbInRecordStatusEnum;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.entity.LockHqbInRecord;
import com.spark.bitrade.entity.LockHqbMemberWallet;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.mapper.dao.LockHqbInRecordMapper;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberTransactionService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.util.IdWorkByTwitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@Service
@Slf4j
public class LockHqbInRecordService extends ServiceImpl<LockHqbInRecordMapper, LockHqbInRecord> implements ILockHqbInRecordService {

    @Autowired
    private IdWorkByTwitter idWorkByTwitter;

    @Autowired
    private ILockHqbMemberWalletService iLockHqbMemberWalletService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LocaleMessageSourceService localeMessageSourceService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "lockHqb", key = "'entity:lockHqb:lockHqbMemberWallet:' + #appId + '-' + #symbol + '-' + #memberId")
    public void hqbTransferInOperation(Long memberId, String appId, String symbol, BigDecimal amount) {

        // 获取活期宝账户
        LockHqbMemberWallet lockHqbMemberWallet = iLockHqbMemberWalletService.findByAppIdAndUnitAndMemberId(appId, symbol, memberId);

        // 获取会员钱包，减少钱包金额
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(symbol, memberId);
        Assert.isTrue(memberWallet.getBalance().compareTo(amount) >= 0, localeMessageSourceService.getMessage("WALLET_BALANCE_INSUFFICIENT"));
        int flag = memberWalletService.deductBalance(memberWallet, amount);
        Assert.isTrue(flag == 1, localeMessageSourceService.getMessage("WALLET_BALANCE_INSUFFICIENT"));

        log.info("================会员" + memberId + "执行活期宝转入操作，减少用户钱包数量成功====================");


        // 新增一条交易记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(amount.negate());
        memberTransaction.setMemberId(memberId);
        memberTransaction.setSymbol(symbol);
        memberTransaction.setType(TransactionType.HQB_ACTIVITY);

        Assert.isTrue(memberTransactionService.save(memberTransaction) != null, localeMessageSourceService.getMessage("WALLET_BALANCE_INSUFFICIENT"));

        log.info("================会员" + memberId + "执行活期宝转入操作，新增交易记录成功====================");


        //增加 活期宝账户 待确认数量 以及 增加累计转入数量
        Boolean updateResult = iLockHqbMemberWalletService.updateWalletByIncrease(lockHqbMemberWallet, amount);
        Assert.isTrue(updateResult, "活期宝转入失败");

        log.info("================会员" + memberId + "执行活期宝转入操作，更新活期宝账户记录成功====================");

        // 新增活期宝转入记录
        LockHqbInRecord lockHqbInRecord = new LockHqbInRecord();
        lockHqbInRecord.setId(idWorkByTwitter.nextId());
        lockHqbInRecord.setAppId(appId);
        lockHqbInRecord.setApplyAmount(amount);
        lockHqbInRecord.setApplyTime(System.currentTimeMillis());
        lockHqbInRecord.setCoinSymbol(symbol);
        lockHqbInRecord.setMemberId(memberId);
        lockHqbInRecord.setStatus(HqbInRecordStatusEnum.UNCONFIRMED);
        lockHqbInRecord.setWalletId(lockHqbMemberWallet.getId());

        Assert.isTrue(this.insert(lockHqbInRecord), "活期宝转入失败");

        log.info("================会员" + memberId + "执行活期宝转入操作，新增活期宝转入记录成功====================");
    }

}
