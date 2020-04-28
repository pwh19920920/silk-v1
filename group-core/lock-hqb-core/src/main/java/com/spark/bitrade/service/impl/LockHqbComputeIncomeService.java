package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.MybatisTransactional;
import com.spark.bitrade.constant.HqbOutRecordStatusEnum;
import com.spark.bitrade.constant.HqbOutRecordTypeEnum;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.LockHqbCoinSettgingMapper;
import com.spark.bitrade.mapper.dao.LockHqbIncomeRecordMapper;
import com.spark.bitrade.mapper.dao.LockHqbMemberWalletMapper;
import com.spark.bitrade.mapper.dao.LockHqbOutRecordMapper;
import com.spark.bitrade.service.MemberTransactionService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.util.IdWorkByTwitter;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class LockHqbComputeIncomeService extends ServiceImpl<LockHqbIncomeRecordMapper, LockHqbIncomeRecord> {

    @Autowired
    private ILockHqbMemberWalletService walletService;
    @Autowired
    private LockHqbCoinSettgingMapper lockHqbCoinSettgingMapper;
    @Autowired
    private IdWorkByTwitter idWorkByTwitter;
    @Autowired
    private LockHqbMemberWalletMapper lockHqbMemberWalletMapper;
    @Autowired
    private ILockHqbThousandsIncomeRecordService lockHqbThousandsIncomeRecordService;
    @Autowired
    private LockHqbOutRecordMapper lockHqbOutRecordMapper;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService memberTransactionService;

    /**
     * 计算收益
     */
    public void computeIncome(Calendar calendar, Long appId, String coinSymbol) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(calendar.getTime());
        log.info("生成[ {} ]万份收益记录", date);
        SpringContextUtil.getBean(LockHqbComputeIncomeService.class).handleThousandIncome(calendar);

        log.info("计算[ {} ]之前的收益", date);
        int current = 1, size = 200;
        List<LockHqbMemberWallet> list;
        do {
            list = lockHqbMemberWalletMapper.selectAsPage(appId, coinSymbol, (current++ - 1) * size, size);
            list.forEach(i -> {
                try {
                    SpringContextUtil.getBean(LockHqbComputeIncomeService.class).computeIncome(i, calendar);
                } catch (Exception e) {
                    log.error("计算收益异常：{}", i.getId(), e);
                }
            });
        } while (list.size() >= size);
    }

    @MybatisTransactional(rollbackFor = Exception.class)
    public void handleThousandIncome(Calendar calendar) {
        EntityWrapper<LockHqbCoinSettging> ew = new EntityWrapper<>();
        ew.setSqlSelect("activity_id, acitivity_name, coin_symbol, app_id");
        ew.groupBy("coin_symbol, app_id");
        List<LockHqbCoinSettging> list = lockHqbCoinSettgingMapper.selectList(ew);
        if (list != null && list.size() > 0) {
            list.forEach(i -> lockHqbThousandsIncomeRecordService.getTenThousandIncomeToday(i.getCoinSymbol(), i.getAppId(), calendar.getTime()));
        }
    }

    /**
     * 单条记录收益计算
     */
    @MybatisTransactional(rollbackFor = Exception.class)
    public void computeIncome(LockHqbMemberWallet wallet, Calendar calendar) {
        EntityWrapper<LockHqbIncomeRecord> ew = new EntityWrapper<>();
        ew.eq("wallet_id", wallet.getId());
        ew.ge("create_time", calendar.getTimeInMillis());

        // 不存在收益记录时则计算收益
        if (selectCount(ew) <= 0) {
            // 计算收益
            LockHqbThousandsIncomeRecord incomeConfig = lockHqbThousandsIncomeRecordService.getTenThousandIncomeToday(wallet.getCoinSymbol(), wallet.getAppId(), calendar.getTime());
            if (incomeConfig != null) {
                BigDecimal thousandsIncome = incomeConfig.getTenThousandIncome();
                // 收益=确认金额*当然万份收益
                BigDecimal incomeAmount = wallet.getLockAmount().multiply(thousandsIncome);
                //modify by qhliao bug小数点超过位数
                incomeAmount.setScale(8, RoundingMode.HALF_UP);
                // 写入收益记录
                LockHqbIncomeRecord record = new LockHqbIncomeRecord();
                record.setAppId(String.valueOf(idWorkByTwitter.nextId()));
                record.setWalletId(wallet.getId());
                record.setMemberId(wallet.getMemberId());
                record.setAppId(wallet.getAppId());
                record.setCoinSymbol(wallet.getCoinSymbol());
                record.setIncomeAmount(incomeAmount);
                record.setCreateTime(calendar.getTimeInMillis());
                insert(record);

                // 更新收益
                lockHqbMemberWalletMapper.updateIncome(wallet.getId(), incomeAmount);
                // 清除缓存
                walletService.clearWalletCache(wallet.getMemberId(), wallet.getAppId(), wallet.getCoinSymbol());
            }
        }
    }

    /**
     * 批量转出
     */
    public void batchTransfer(String coinSymbol) {
        int current = 1, size = 200;
        List<LockHqbMemberWallet> list;
        do {
            list = lockHqbMemberWalletMapper.selectBatchTransferPage(coinSymbol, (current++ - 1) * size, size);
            list.forEach(i -> {
                try {
                    SpringContextUtil.getBean(LockHqbComputeIncomeService.class).transfer(i);
                } catch (Exception e) {
                    log.error("活期宝批量转出异常：{}", i.getId(), e);
                }
            });
        } while (list.size() >= size);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transfer(LockHqbMemberWallet wallet) {
        // 新增活期宝转出记录
        LockHqbOutRecord lockHqbOutRecord = new LockHqbOutRecord();
        lockHqbOutRecord.setId(idWorkByTwitter.nextId());
        lockHqbOutRecord.setAppId("0");
        lockHqbOutRecord.setApplyAmount(wallet.getPlanInAmount().add(wallet.getLockAmount()));
        lockHqbOutRecord.setCreateTime(System.currentTimeMillis());
        lockHqbOutRecord.setCoinSymbol(wallet.getCoinSymbol());
        lockHqbOutRecord.setMemberId(wallet.getMemberId());
        lockHqbOutRecord.setStatus(HqbOutRecordStatusEnum.COMPLETED);
        lockHqbOutRecord.setType(HqbOutRecordTypeEnum.IMMEDIATELY);
        lockHqbOutRecord.setWalletId(wallet.getId());
        lockHqbOutRecordMapper.insert(lockHqbOutRecord);

        int size = lockHqbMemberWalletMapper.updateWalletByDecrease(wallet, lockHqbOutRecord.getApplyAmount());
        Assert.isTrue(size > 0, "转出失败，账户可用余额应大于转出金额");

        // 获取会员钱包，增加钱包金额
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(wallet.getCoinSymbol(), wallet.getMemberId());
        Assert.notNull(memberWallet, "没有找到对应用户钱包");
        memberWalletService.increaseBalance(memberWallet.getId(), lockHqbOutRecord.getApplyAmount());

        // 新增一条交易记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(lockHqbOutRecord.getApplyAmount());
        memberTransaction.setMemberId(wallet.getMemberId());
        memberTransaction.setSymbol(wallet.getCoinSymbol());
        memberTransaction.setType(TransactionType.HQB_ACTIVITY);
        memberTransactionService.save(memberTransaction);
    }
}
