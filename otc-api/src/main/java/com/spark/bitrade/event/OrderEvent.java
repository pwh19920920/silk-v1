package com.spark.bitrade.event;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.coin.CoinExchangeFactory;
import com.spark.bitrade.constant.PromotionRewardType;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.dao.MemberDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.service.RewardPromotionSettingService;
import com.spark.bitrade.service.RewardRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.spark.bitrade.util.BigDecimalUtils.*;

/**
 * @author Zhang Jinwei
 * @date 2018年01月22日
 */
@Service
public class OrderEvent {
    @Autowired
    //private MemberDao memberDao;
    private MemberService memberService;
    /*@Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private CoinExchangeFactory coins;*/

    @Async
    public void onOrderCompleted(Order order) {
        //更新交易次数
        Member merchantMember = memberService.findOne(order.getMemberId());
        //merchantMember.setTransactions(merchantMember.getTransactions() + 1);
        //memberService.save(merchantMember);
        memberService.updateTransactionsTime(merchantMember.getId());

        Member userMember = memberService.findOne(order.getCustomerId());
        //userMember.setTransactions(userMember.getTransactions() + 1);
        //memberService.save(userMember);
        memberService.updateTransactionsTime(userMember.getId());


        //del by yangch 时间： 2018.05.17 原因：取消用户注册2级反币功能
        /*Member member = memberDao.findOne(order.getMemberId());
        member.setTransactions(member.getTransactions() + 1);
        Member member1 = memberDao.findOne(order.getCustomerId());
        member1.setTransactions(member1.getTransactions() + 1);
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.TRANSACTION);
        //if (rewardPromotionSetting != null) { //edit by yangch 时间： 2018.04.26 原因：合并代码
        if (rewardPromotionSetting != null && coins.get("USDT").compareTo(BigDecimal.ZERO) > 0) {
            Member[] array = {member, member1};
            Arrays.stream(array).forEach(
                    x -> {
                        //如果要根据某个时间来返佣金，把下面这行替换一下
                        //if (x.getInviterId() != null&&!(DateUtil.diffDays(new Date(), x.getRegistrationTime()) > rewardPromotionSetting.getEffectiveTime())) {
                        //只有首次交易获得佣金
                        if (x.getTransactions() == 1 && x.getInviterId() != null) {
                            Member member2 = memberDao.findOne(x.getInviterId());
                            MemberWallet memberWallet1 = memberWalletService.findByCoinAndMember(rewardPromotionSetting.getCoin(), member2);
                            BigDecimal number = mulRound(order.getNumber(), div(coins.get(order.getCoin().getUnit()), coins.get("USDT"))); //add by yangch 时间： 2018.04.26 原因：代码合并
                            //BigDecimal amount1 = mulRound(order.getNumber(), getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one")));
                            BigDecimal amount1 = mulRound(number, getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one")));
                            memberWallet1.setBalance(add(memberWallet1.getBalance(), amount1));
                            memberWalletService.save(memberWallet1);
                            RewardRecord rewardRecord1 = new RewardRecord();
                            rewardRecord1.setAmount(amount1);
                            rewardRecord1.setCoin(rewardPromotionSetting.getCoin());
                            rewardRecord1.setMember(member2);
                            rewardRecord1.setRemark(rewardPromotionSetting.getType().getCnName());
                            rewardRecord1.setType(RewardRecordType.PROMOTION);
                            rewardRecordService.save(rewardRecord1);
                            if (member2.getInviterId() != null) {
                                Member member3 = memberDao.findOne(member2.getInviterId());
                                MemberWallet memberWallet2 = memberWalletService.findByCoinAndMember(rewardPromotionSetting.getCoin(), member3);
                                //edit by yangch 时间： 2018.04.26 原因：代码合并
                                //BigDecimal amount2 = mulRound(order.getNumber(), getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two")));
                                BigDecimal amount2 = mulRound(number, getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two")));
                                memberWallet2.setBalance(add(memberWallet2.getBalance(), amount2));
                                RewardRecord rewardRecord2 = new RewardRecord();
                                rewardRecord2.setAmount(amount2);
                                rewardRecord2.setCoin(rewardPromotionSetting.getCoin());
                                rewardRecord2.setMember(member3);
                                rewardRecord2.setRemark(rewardPromotionSetting.getType().getCnName());
                                rewardRecord2.setType(RewardRecordType.PROMOTION);
                                rewardRecordService.save(rewardRecord2);
                            }
                        }
                    }
            );
        }*/
    }
}
