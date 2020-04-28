package com.spark.bitrade.service;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

/***
 * 推荐返佣
 * @author yangch
 * @time 2018.07.02 10:13
 */
@Service
@Slf4j
public class PromoteRewardService {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private RewardRecordService rewardRecordService;

    @Autowired
    private CoinService coinService;


    /**
     * 交易手续费返佣金
     *
     * @param refTransaction    关联单号（引用的手续费）
     * @param member      订单拥有者
     * @param incomeSymbol 币种
     */
    @Async("reward")
    public void asycPromoteReward(MemberTransaction refTransaction, Member member, String incomeSymbol){
        getService().promoteReward(refTransaction, member, incomeSymbol);
    }

    /**
     * 交易手续费返佣金
     *
     * @param refTransaction    关联单号（引用的手续费）
     * @param member      订单拥有者
     * @param incomeSymbol 币种
     */
    @Transactional(rollbackFor = Exception.class)
    public void promoteReward(MemberTransaction refTransaction, Member member, String incomeSymbol) {
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.EXCHANGE_TRANSACTION);
        if (rewardPromotionSetting != null && member.getInviterId() != null) {
            if (!(DateUtil.diffDays(new Date(), member.getRegistrationTime()) > rewardPromotionSetting.getEffectiveTime())) {
                JSONObject jsonObject = JSONObject.parseObject(rewardPromotionSetting.getInfo());

                BigDecimal rewardRate1 = jsonObject.getBigDecimal("one");
                if(rewardRate1 == null || rewardRate1.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("没有配置一级推荐返佣");
                    return ;
                }

                BigDecimal fee = refTransaction.getFee();   //获取手续费
                Member member1 = memberService.findOne(member.getInviterId());
                if(member1 == null) {
                    log.info("没有找到{}帐号的推荐用户", member.getId());
                    return ;
                }
                if(member1.getStatus()== CommonStatus.ILLEGAL) {
                    log.info("{}帐号已经被禁用，不发放佣金", member1.getId());
                } else {
                    BigDecimal rewardAmount1 = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(rewardRate1), 8);
                    savePromoteReward(refTransaction, member1, incomeSymbol, rewardAmount1, rewardPromotionSetting, RewardRecordLevel.ONE);
                }

                //二级返佣
                //if (member1.getInviterId() != null && !(DateUtil.diffDays(new Date(), member1.getRegistrationTime()) > rewardPromotionSetting.getEffectiveTime())) {
                if (member1.getInviterId() != null ) {
                    BigDecimal rewardRate2 = jsonObject.getBigDecimal("two");
                    if(rewardRate2 == null || rewardRate2.compareTo(BigDecimal.ZERO) <= 0) {
                        log.info("没有配置二级推荐返佣");
                        return ;
                    }

                    Member member2 = memberService.findOne(member1.getInviterId());
                    if(member2 == null) {
                        log.info("没有找到{}帐号的推荐用户", member1.getId());
                        return ;
                    }
                    if(member2.getStatus()== CommonStatus.ILLEGAL) {
                        log.info("{}帐号已经被禁用", member2.getId());
                    } else {
                        BigDecimal rewardAmount2 = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(rewardRate2), 8);
                        savePromoteReward(refTransaction, member2, incomeSymbol,rewardAmount2, rewardPromotionSetting, RewardRecordLevel.TWO);
                    }

                    //三级返佣
                    if (member2.getInviterId() != null ) {
                        BigDecimal rewardRate3 = jsonObject.getBigDecimal("three");
                        if (rewardRate3 == null || rewardRate3.compareTo(BigDecimal.ZERO) <= 0) {
                            log.info("没有配置三级推荐返佣");
                            return;
                        }

                        Member member3 = memberService.findOne(member2.getInviterId());
                        if (member3 == null) {
                            log.info("没有找到{}帐号的推荐用户", member2.getId());
                            return;
                        }
                        if (member3.getStatus() == CommonStatus.ILLEGAL) {
                            log.info("{}帐号已经被禁用", member3.getId());
                        } else {
                            BigDecimal rewardAmount3 = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(rewardRate3), 8);
                            savePromoteReward(refTransaction, member3, incomeSymbol, rewardAmount3, rewardPromotionSetting, RewardRecordLevel.THREE);
                        }

                    }
                }
            } else {
                log.warn("推荐返佣活动已过期");
            }
        } else {
            log.warn("没有配置推荐返佣");
        }
    }
    //add by yangch 时间： 2018.05.15 原因：添加返佣记录
    /** 
      * @author yangch
      * @time 2018.05.15 18:38
     *  @param refTransaction    关联单号（member_transaction表）
     *  @param promoteMember 返佣用户
     *   @param rewardAmount 返佣金额
     *   @param incomeSymbol 币种
     *   @param rewardPromotionSetting 返佣配置
     *   @param rewardRecordLevel 返佣级别
     * @return 
     */
    public void savePromoteReward(MemberTransaction refTransaction,Member promoteMember, String incomeSymbol ,
                                  BigDecimal rewardAmount, RewardPromotionSetting rewardPromotionSetting, RewardRecordLevel rewardRecordLevel){
        //Member promoteMember = memberService.findOne(transactionMember.getInviterId());
        //BigDecimal reward1 = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(jsonObject.getBigDecimal("two")), 8);

        if (rewardAmount.compareTo(BigDecimal.ZERO) > 0 && rewardPromotionSetting!=null) {
            MemberWallet promoteMemberWalletCeche = memberWalletService.findCacheByCoinUnitAndMemberId(incomeSymbol, promoteMember.getId());
            if(promoteMemberWalletCeche == null){
                //对应币种的账户不存在，则创建对应的账户（解决买币账户不存在的问题）
                Coin coin = coinService.findByUnit(incomeSymbol);
                if(null == coin){
                    log.warn("交易币种不存在。币种名称={}", incomeSymbol);
                    return;
                }
                promoteMemberWalletCeche = memberWalletService.createMemberWallet(promoteMember.getId(), coin);
                if(null == promoteMemberWalletCeche){
                    log.warn("用户账户不存在。用户id={},币种名称={}",promoteMember.getId(),  incomeSymbol);
                    return;
                }
            }
            //钱包账户和会员钱包账户是否一致
            if(promoteMemberWalletCeche.getMemberId().longValue() != promoteMember.getId().longValue()){
                log.warn("钱包账户获取有误。用户id={},钱包={}",promoteMember.getId(),  promoteMemberWalletCeche);
                return;
            }
            //钱包的币种和返佣币种是否一致
            if(promoteMemberWalletCeche.getCoin()!=null
                    && !promoteMemberWalletCeche.getCoin().getUnit().equals(incomeSymbol)){
                log.warn("获取的返币钱包账户有误。用户id={},钱包={}",promoteMember.getId(),  promoteMemberWalletCeche);
                return;
            }

            //添加返佣
            //promoteMemberWallet.setBalance(BigDecimalUtils.add(promoteMemberWallet.getBalance(), rewardAmount));
            MessageResult result = memberWalletService.increaseBalance(promoteMemberWalletCeche.getId(),rewardAmount);

            //先添加返佣记录
            RewardRecord rewardRecord = new RewardRecord();
            rewardRecord.setAmount(rewardAmount);
            rewardRecord.setCoin(promoteMemberWalletCeche.getCoin());
            rewardRecord.setMember(promoteMember);
            rewardRecord.setRemark(rewardPromotionSetting.getType().getCnName());
            rewardRecord.setType(RewardRecordType.PROMOTION);
            rewardRecord.setRefTransactionId(refTransaction.getId());  //关联的是 member_transaction 表中类型“3=EXCHANGE(币币交易)”对应的交易记录ID
            rewardRecord.setLevel(rewardRecordLevel);
            rewardRecord.setTreatedTime(new Date());
            rewardRecord.setFromMemberId(refTransaction.getMemberId());
            rewardRecord.setFromAmount(rewardAmount);
            rewardRecord.setFromCoinUnit(promoteMemberWalletCeche.getCoin().getUnit());
            rewardRecord.setExchangeRate(BigDecimal.valueOf(1L)); //未进行币种转化，所以汇率为1
            rewardRecord.setRewardCycle(rewardPromotionSetting.getRewardCycle());
            if(result.getCode()==0) { //返佣成功，标记为已发放
                rewardRecord.setStatus(RewardRecordStatus.TREATED); //已发放
            } else { //返佣失败，标记为 未发放
                rewardRecord.setStatus(RewardRecordStatus.UNTREATED); //未发放
            }
            RewardRecord rewardRecordNew = rewardRecordService.save(rewardRecord);

            if(result.getCode()==0) { //返佣成功，添加记录
                //再添加交易记录
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(rewardAmount);
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction.setMemberId(promoteMember.getId());
                memberTransaction.setSymbol(incomeSymbol);
                //memberTransaction.setType(TransactionType.PROMOTION_AWARD);
                memberTransaction.setType(TransactionType.EXCHANGE_PROMOTION_AWARD);    //交易记录为“12=EXCHANGE_PROMOTION_AWARD(币币交易返佣奖励)”
                memberTransaction.setRefId(String.valueOf(rewardRecordNew.getId()));        //关联“reward_record”表中对应的返佣记录
                transactionService.save(memberTransaction);
            }

        } else{
            log.warn("返佣数量小于0或者没有3级返佣规则配置");
        }
    }

    private PromoteRewardService getService(){
        return SpringContextUtil.getBean(this.getClass());
    }

}
