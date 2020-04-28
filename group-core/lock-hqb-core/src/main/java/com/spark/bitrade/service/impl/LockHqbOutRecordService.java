package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.constant.HqbOutRecordStatusEnum;
import com.spark.bitrade.constant.HqbOutRecordTypeEnum;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.entity.LockHqbMemberWallet;
import com.spark.bitrade.entity.LockHqbOutRecord;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.mapper.dao.LockHqbOutRecordMapper;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberTransactionService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.util.IdWorkByTwitter;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@Service
@Slf4j
public class LockHqbOutRecordService extends ServiceImpl<LockHqbOutRecordMapper, LockHqbOutRecord> implements ILockHqbOutRecordService {
    @Autowired
    private IdWorkByTwitter idWorkByTwitter;

    @Autowired
    private ILockHqbMemberWalletService iLockHqbMemberWalletService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "lockHqb", key = "'entity:lockHqb:lockHqbMemberWallet:' + #appId + '-' + #symbol + '-' + #memberId")
    public void hqbTransferOutOperation(Long memberId, String appId, String symbol, BigDecimal amount) {
        // 获取活期宝账户
        LockHqbMemberWallet lockHqbMemberWallet = iLockHqbMemberWalletService.findByAppIdAndUnitAndMemberId(appId, symbol, memberId);

        // 新增活期宝转出记录
        LockHqbOutRecord lockHqbOutRecord = new LockHqbOutRecord();
        lockHqbOutRecord.setId(idWorkByTwitter.nextId());
        lockHqbOutRecord.setAppId(appId);
        lockHqbOutRecord.setApplyAmount(amount);
        lockHqbOutRecord.setCreateTime(System.currentTimeMillis());
        lockHqbOutRecord.setCoinSymbol(symbol);
        lockHqbOutRecord.setMemberId(memberId);
        lockHqbOutRecord.setStatus(HqbOutRecordStatusEnum.COMPLETED);
        lockHqbOutRecord.setType(HqbOutRecordTypeEnum.IMMEDIATELY);
        lockHqbOutRecord.setWalletId(lockHqbMemberWallet.getId());

        Assert.isTrue(this.insert(lockHqbOutRecord), msService.getMessage("TRANS_OUT_FAILED"));
        log.info("================会员" + memberId + "执行活期宝转出操作，新增活期宝转出记录成功====================");


        Boolean updateResult = iLockHqbMemberWalletService.updateWalletByDecrease(lockHqbMemberWallet, amount);

        Assert.isTrue(updateResult, msService.getMessage("TRANS_OUT_FAILED_BALANCE_INSUFFICIENT"));
        log.info("================会员" + memberId + "执行活期宝转出操作，更新活期宝账户记录成功====================");

        // 获取会员钱包，增加钱包金额
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(symbol, memberId);
        MessageResult messageResult = memberWalletService.increaseBalance(memberWallet.getId(), amount);

        Assert.isTrue(messageResult.isSuccess(), msService.getMessage("TRANS_OUT_FAILED"));
        log.info("================会员" + memberId + "执行活期宝转出操作，增加用户钱包数量成功====================");


        // 新增一条交易记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(amount);
        memberTransaction.setMemberId(memberId);
        memberTransaction.setSymbol(symbol);
        //交易记录为“20=QUANTIFY_ACTIVITY(量化投资)”
        memberTransaction.setType(TransactionType.HQB_ACTIVITY);

        Assert.isTrue(memberTransactionService.save(memberTransaction) != null, msService.getMessage("TRANS_OUT_FAILED"));

        log.info("================会员" + memberId + "执行活期宝转出操作，新增交易记录成功====================");
    }

}
