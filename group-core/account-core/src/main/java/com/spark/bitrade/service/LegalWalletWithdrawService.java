package com.spark.bitrade.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.constant.WithdrawStatus;
import com.spark.bitrade.dao.LegalWalletWithdrawDao;
import com.spark.bitrade.dao.MemberWalletDao;
import com.spark.bitrade.entity.LegalWalletWithdraw;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.entity.QLegalWalletWithdraw;
import com.spark.bitrade.service.Base.TopBaseService;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
public class LegalWalletWithdrawService extends TopBaseService<LegalWalletWithdraw, LegalWalletWithdrawDao> {
    @Autowired
    private LegalWalletWithdrawDao legalWalletWithdrawDao;
    //add|edit|del by tansitao 时间： 2018/5/17 原因：增加钱包操作service
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberWalletDao memberWalletDao;

    @Autowired
    public void setDao(LegalWalletWithdrawDao legalWalletWithdrawDao) {
        super.setDao(super.dao = legalWalletWithdrawDao);
    }

    public LegalWalletWithdraw findOne(Long id) {
        return legalWalletWithdrawDao.findOne(id);
    }

    //审核通过
    public void pass(LegalWalletWithdraw legalWalletWithdraw) {
        legalWalletWithdraw.setStatus(WithdrawStatus.WAITING);
        legalWalletWithdraw.setDealTime(new Date());//处理时间
        legalWalletWithdrawDao.save(legalWalletWithdraw);
    }

    public LegalWalletWithdraw findDetailWeb(Long id, Long memberId) {
        BooleanExpression and = QLegalWalletWithdraw.legalWalletWithdraw.id.eq(id)
                .and(QLegalWalletWithdraw.legalWalletWithdraw.member.id.eq(memberId));
        return legalWalletWithdrawDao.findOne(and);
    }

    //提现
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(MemberWallet wallet, LegalWalletWithdraw legalWalletWithdraw) {
        //edit by tansitao 时间： 2018/5/17 原因：修改钱包余额、冻结余额
//        wallet.setBalance(BigDecimalUtils.sub(wallet.getBalance(), legalWalletWithdraw.getAmount()));
//        wallet.setFrozenBalance(BigDecimalUtils.add(wallet.getFrozenBalance(), legalWalletWithdraw.getAmount()));
        memberWalletService.freezeBalance(wallet, legalWalletWithdraw.getAmount());

        legalWalletWithdrawDao.save(legalWalletWithdraw);
    }

    //提现不通过
    @Transactional(rollbackFor = Exception.class)
    public void noPass(MemberWallet wallet, LegalWalletWithdraw withdraw) {
        //edit by tansitao 时间： 2018/5/17 原因：修改钱包余额、冻结余额
//        wallet.setFrozenBalance(BigDecimalUtils.sub(wallet.getFrozenBalance(), withdraw.getAmount()));//冻结金额减少
//        wallet.setBalance(BigDecimalUtils.add(wallet.getBalance(), withdraw.getAmount()));//本金增加
        memberWalletService.thawBalance(wallet,withdraw.getAmount());
        withdraw.setStatus(WithdrawStatus.FAIL);//标记失败
        withdraw.setDealTime(new Date());//处理时间
    }

    //打款
    @Transactional(rollbackFor = Exception.class)
    public void remit(String paymentInstrument, LegalWalletWithdraw withdraw, MemberWallet wallet) {
        withdraw.setPaymentInstrument(paymentInstrument);//支付凭证
        withdraw.setStatus(WithdrawStatus.SUCCESS);//标记成功
        withdraw.setRemitTime(new Date());//打款时间
        //edit by tansitao 时间： 2018/5/17 原因：修改钱包冻结余额
//        wallet.setFrozenBalance(BigDecimalUtils.sub(wallet.getFrozenBalance(), withdraw.getAmount()));//钱包冻结金额减少
        int ret = memberWalletDao.decreaseFrozen(wallet.getId(), withdraw.getAmount());
        if (ret == 0)
        {
            MessageResult.error("Information Expired");
        }
    }
}
