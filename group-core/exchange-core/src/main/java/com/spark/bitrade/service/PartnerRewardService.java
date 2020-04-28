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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

/***
 * 合伙人返佣
 * @author yangch
 * @time 2018.07.02 10:18
 */
@Service
@Slf4j
public class PartnerRewardService {
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private PartnerAreaService partnerAreaService;

    @Autowired
    private GyDmcodeService gyDmcodeService;

    /**
     * 交易手续费返佣金给合伙人
     *
     * @param refTransaction    关联单号（引用的手续费）
     * @param member      订单拥有者
     * @param order 委托订单
     * @param  trade 交易详情
     * @param exchangeCoin 交易对信息
     */
    @Async("reward")
    public void asyncPartnerReward(MemberTransaction refTransaction, Member member, final ExchangeOrder order ,
                                   final ExchangeTrade trade, final ExchangeCoin exchangeCoin){
        getService().partnerReward(refTransaction, member, order , trade, exchangeCoin);
    }

    /**
     * 交易手续费返佣金给合伙人
     *
     * @param refTransaction    关联单号（引用的手续费）
     * @param member      订单拥有者
     * @param order 委托订单
     * @param  trade 交易详情
     * @param exchangeCoin 交易对信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void partnerReward(MemberTransaction refTransaction, Member member, final ExchangeOrder order ,
                              final ExchangeTrade trade, final ExchangeCoin exchangeCoin) {
        if(!StringUtils.hasText(member.getAreaId())) {
            log.info("{}用户没有归属区域",member.getId());
            return;
        }

        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.PARTNER);
        if (rewardPromotionSetting != null) {
            if (rewardPromotionSetting.getEffectiveTime() ==0 ||
                    (DateUtil.diffDays(new Date(), rewardPromotionSetting.getUpdateTime()) <= rewardPromotionSetting.getEffectiveTime())) {
                JSONObject jsonObject = JSONObject.parseObject(rewardPromotionSetting.getInfo());
                if(null == jsonObject){
                    log.warn("未能正确解析合伙人返佣配置规则");
                    return;
                }

                BigDecimal fee = refTransaction.getFee();       //币币交易手续费
                boolean hasReward1=false,hasReward2=false;   //标记是否有返佣

                //区域第一级合伙人返佣（正常是区县级）
                DimArea dimArea1 =  gyDmcodeService.findOneByDmCode(member.getAreaId()); //交易用户的区域
                if(dimArea1 == null) {
                    log.warn("{}用户的归属区域不存在",member.getId());
                    return;
                }

                BigDecimal rewardAmount1 = BigDecimal.ZERO;
                BigDecimal rewardRate1 = jsonObject.getBigDecimal("one");
                if(rewardRate1 == null || rewardRate1.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("没有配置一级合伙人返佣规则");
                } else {
                    rewardAmount1 = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(rewardRate1), 8); //合伙人佣金

                    PartnerArea partnerArea1 = partnerAreaService.findPartnerAreaByAreaId(dimArea1);
                    if(partnerArea1 == null){
                        log.info("没有找到{}区域的合伙人",dimArea1.getAreaId());
                    } else {
                        Member member1 = partnerArea1.getMember();  //区域合伙人
                        if(member1 == null) {
                            log.info("{}区域没有设置合伙人",dimArea1.getAreaId());
                        } else {
                            if (member1.getStatus() == CommonStatus.ILLEGAL) {
                                log.info("{}区域的合伙人{}帐号已经被禁用，不发放合伙人佣金",dimArea1.getAreaId(), member1.getId());
                            } else {
                                savePartnerReward(rewardPromotionSetting, rewardAmount1, RewardRecordLevel.ONE,refTransaction, member1, order , trade ,  exchangeCoin);
                                hasReward1 = true;
                            }
                        }
                    }
                }


                //区域第二级合伙人返佣（正常是市级）
                DimArea dimArea2 =  gyDmcodeService.findOneByDmCode(dimArea1.getFatherId());
                if(dimArea2 == null) {
                    log.warn("{}区域的归属区域不存在",dimArea1.getAreaId());
                    return;
                }

                BigDecimal rewardAmount2 = BigDecimal.ZERO;
                BigDecimal rewardRate2 = jsonObject.getBigDecimal("two");
                if(rewardRate2 == null || rewardRate2.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("没有配置二级合伙人返佣规则");
                } else {
                    rewardAmount2 = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(rewardRate2), 8);
                    if(!hasReward1) {
                        rewardAmount2 = rewardAmount2.add(rewardAmount1);   //如果第一级没有合伙人，则将佣金返给第二级合伙人
                    }

                    PartnerArea partnerArea2 = partnerAreaService.findPartnerAreaByAreaId(dimArea2);
                    if (partnerArea2 == null ) {
                        log.info("没有找到{}区域的合伙人", dimArea2.getAreaId());
                    } else {
                        Member member2 = partnerArea2.getMember();
                        if(member2 == null) {
                            log.info("{}区域没有设置合伙人",dimArea2.getAreaId());
                        } else {
                            if (member2.getStatus() == CommonStatus.ILLEGAL) {
                                log.info("{}区域的合伙人{}帐号已经被禁用，不发放合伙人佣金",dimArea2.getAreaId(), member2.getId());
                            } else {
                                savePartnerReward(rewardPromotionSetting, rewardAmount2, RewardRecordLevel.TWO,refTransaction, member2, order , trade ,  exchangeCoin);
                                hasReward2 = true;
                            }
                        }
                    }
                }


                //区域第三级合伙人返佣（正常是省级）
                DimArea dimArea3 =  gyDmcodeService.findOneByDmCode(dimArea2.getFatherId());
                if(dimArea3 == null) {
                    log.warn("{}区域的归属区域不存在",dimArea2.getAreaId());
                    return;
                }

                BigDecimal rewardAmount3 = BigDecimal.ZERO;
                BigDecimal rewardRate3 = jsonObject.getBigDecimal("three");
                if(rewardRate3 == null || rewardRate3.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("没有配置三级合伙人返佣规则");
                    return ;
                } else {
                    rewardAmount3 = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(rewardRate3), 8);
                    if(!hasReward2) {
                        rewardAmount3 = rewardAmount3.add(rewardAmount2);   //如果第二级没有合伙人，则将佣金返给第三级合伙人
                    }

                    PartnerArea partnerArea3 = partnerAreaService.findPartnerAreaByAreaId(dimArea3);
                    if (partnerArea3 == null ) {
                        log.info("没有找到{}区域的合伙人", dimArea3.getAreaId());
                    } else {
                        Member member3 = partnerArea3.getMember();
                        if(member3 == null) {
                            log.info("{}区域没有设置合伙人",dimArea3.getAreaId());
                        } else {
                            if (member3.getStatus() == CommonStatus.ILLEGAL) {
                                log.info("{}区域的合伙人{}帐号已经被禁用，不发放合伙人佣金",dimArea3.getAreaId(), member3.getId());
                            } else {
                                savePartnerReward(rewardPromotionSetting, rewardAmount3, RewardRecordLevel.THREE, refTransaction, member3, order, trade , exchangeCoin);
                            }
                        }
                    }
                }

            } else {
                log.warn("区域合伙人返佣规则已过期");
            }
        } else {
            log.debug("没有区域合伙人返佣规则");
        }
    }

    //add by yangch 时间： 2018.05.30 原因：添加合伙人返佣记录
    /** 
      * @author yangch
      * @time 2018-05-30 11:22:49
     *  @param rewardPromotionSetting 返佣配置
     *  @param rewardAmount 返佣金额
     *  @param rewardRecordLevel 返佣级别
     *  @param refTransaction    关联单号（member_transaction表）
     *  @param partnerMember 合伙人用户
     *   //@param incomeSymbol 币种
     *   @param order 委托订单
     *   @param trade 交易详情
     *   @param exchangeCoin 交易对信息
     * @return 
     */
    public void savePartnerReward(final RewardPromotionSetting rewardPromotionSetting, BigDecimal rewardAmount,RewardRecordLevel rewardRecordLevel,
                                  MemberTransaction refTransaction,final Member partnerMember, final ExchangeOrder order ,
                                  final ExchangeTrade trade, final ExchangeCoin exchangeCoin){
        if (rewardAmount.compareTo(BigDecimal.ZERO) > 0 ) {
            String incomeSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getCoinSymbol() : order.getBaseSymbol();
            if(rewardPromotionSetting.getRewardCycle() == PromotionRewardCycle.REALTIME) { //实时返佣
                MemberWallet promoteMemberWalletCeche = memberWalletService.findCacheByCoinUnitAndMemberId(incomeSymbol, partnerMember.getId());
                if(promoteMemberWalletCeche == null){
                    //对应币种的账户不存在，则创建对应的账户（解决买币账户不存在的问题）
                    Coin coin = coinService.findByUnit(incomeSymbol);
                    if(null == coin){
                        log.warn("交易币种不存在。 币种名称={}", incomeSymbol);
                        return;
                    }
                    promoteMemberWalletCeche = memberWalletService.createMemberWallet(partnerMember.getId(), coin);
                    if(null == promoteMemberWalletCeche){
                        log.warn("用户账户不存在。用户id={},币种名称={}",partnerMember.getId(),  incomeSymbol);
                        return;
                    }
                }
                //钱包账户和会员钱包账户是否一致
                if(promoteMemberWalletCeche.getMemberId().longValue() != partnerMember.getId().longValue()){
                    log.warn("钱包账户获取有误。用户id={},钱包={}",partnerMember.getId(),  promoteMemberWalletCeche);
                    return;
                }
                //钱包的币种和返佣币种是否一致
                if(promoteMemberWalletCeche.getCoin()!=null
                        && !promoteMemberWalletCeche.getCoin().getUnit().equals(incomeSymbol)){
                    log.warn("获取的返币钱包账户有误。用户id={},钱包={}",partnerMember.getId(),  promoteMemberWalletCeche);
                    return;
                }

                //添加返佣
                MessageResult result = memberWalletService.increaseBalance(promoteMemberWalletCeche.getId(), rewardAmount);

                //先添加返佣记录
                RewardRecordStatus rewardRecordStatus;
                if(result.getCode()==0) { //返佣成功，标记为已发放
                    rewardRecordStatus=RewardRecordStatus.TREATED; //已发放
                } else { //返佣失败，标记为 未发放
                    rewardRecordStatus=RewardRecordStatus.UNTREATED; //未发放
                }
                //保存 不需要兑换的未发放佣金记录
                RewardRecord rewardRecordNew = saveNoExchangeRewardRecord4Partner(rewardPromotionSetting, rewardAmount,rewardRecordLevel
                        ,rewardRecordStatus, refTransaction,partnerMember,promoteMemberWalletCeche.getCoin());

                if(result.getCode()==0) { //返佣成功，添加记录
                    //再添加交易记录
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setAmount(rewardAmount);
                    memberTransaction.setFee(BigDecimal.ZERO);
                    memberTransaction.setFeeDiscount(BigDecimal.ZERO);
                    memberTransaction.setMemberId(partnerMember.getId());
                    memberTransaction.setSymbol(incomeSymbol);
                    memberTransaction.setType(TransactionType.EXCHANGE_PARTNER_AWARD);    //交易记录为“13=EXCHANGE_PARTNER_AWARD("币币交易合伙人奖励")”
                    memberTransaction.setRefId(String.valueOf(rewardRecordNew.getId()));      //关联“reward_record”表中对应的返佣记录
                    transactionService.save(memberTransaction);
                }
            } else {  //周期返佣，只记录返佣记录即可
                if(rewardPromotionSetting.getRewardCoin() == PromotionRewardCoin.REWARDSOURCECOIN
                        || rewardPromotionSetting.getRewardCoin() == PromotionRewardCoin.REWARDSLB ) {   //暂时不支持丝路币的退回，按实时返佣规则处理
                    //查询币种
                    Coin coin = coinService.findByUnit(incomeSymbol);

                    //保存 不需要兑换的未发放佣金记录
                    saveNoExchangeRewardRecord4Partner(rewardPromotionSetting, rewardAmount,rewardRecordLevel
                            ,RewardRecordStatus.UNTREATED, refTransaction,partnerMember,coin);
                } else if(rewardPromotionSetting.getRewardCoin() == PromotionRewardCoin.REWARDUSDT) {
                    if(order.getDirection() == ExchangeOrderDirection.SELL) { //卖单是以basecoin进行返佣，不需要兑换
                        Coin coin = coinService.findByUnit(incomeSymbol);
                        //保存 不需要兑换的未发放佣金记录
                        saveNoExchangeRewardRecord4Partner(rewardPromotionSetting, rewardAmount,rewardRecordLevel
                                ,RewardRecordStatus.UNTREATED, refTransaction,partnerMember,coin);
                    } else {
                        //币种兑换，其他币种兑换为基本，同时实时汇率即为交易价
                        Coin coin = coinService.findByUnit(order.getBaseSymbol());
                        BigDecimal exchangeRewardAmount = rewardAmount.multiply(trade.getPrice()).setScale(exchangeCoin.getBaseCoinScale(),BigDecimal.ROUND_DOWN);

                        //添加返佣记录
                        RewardRecord rewardRecord = new RewardRecord();
                        rewardRecord.setAmount(exchangeRewardAmount);
                        rewardRecord.setCoin(coin);
                        rewardRecord.setMember(partnerMember);
                        rewardRecord.setRemark(rewardPromotionSetting.getType().getCnName());
                        rewardRecord.setType(RewardRecordType.PARTNER); //合伙人推广返佣
                        rewardRecord.setRefTransactionId(refTransaction.getId());  //关联的是 member_transaction 表中类型“3=EXCHANGE(币币交易)”对应的交易记录ID
                        rewardRecord.setLevel(rewardRecordLevel);
                        rewardRecord.setTreatedTime(null); //不设置返佣时间
                        rewardRecord.setFromMemberId(refTransaction.getMemberId());
                        rewardRecord.setFromAmount(rewardAmount);
                        rewardRecord.setFromCoinUnit(incomeSymbol);
                        rewardRecord.setExchangeRate(trade.getPrice()); //汇率即为交易价
                        rewardRecord.setStatus(RewardRecordStatus.UNTREATED); //未发放
                        rewardRecord.setRewardCycle(rewardPromotionSetting.getRewardCycle());
                        rewardRecordService.save(rewardRecord);
                    }
                } else {
                    log.warn("无效的兑换规则：兑换为{}",rewardPromotionSetting.getRewardCoin().getCnName());
                }
            }
        }else {
            log.warn("合伙人返佣数量小于等于0");
        }
    }

    /** 
     * 保存 不需要兑换的放佣金记录
     * @author yangch
     * @time 2018.05.15 18:38
     *  @param rewardPromotionSetting 返佣配置
     *  @param rewardAmount 返佣金额
     *  @param rewardRecordLevel 返佣级别
     *  @param rewardRecordStatus 发放状态
     *  @param refTransaction    关联单号（member_transaction表）
     *  @param partnerMember 合伙人用户
     * @return 
     */
    public RewardRecord saveNoExchangeRewardRecord4Partner(
            final RewardPromotionSetting rewardPromotionSetting,BigDecimal rewardAmount,
            RewardRecordLevel rewardRecordLevel,RewardRecordStatus rewardRecordStatus,
            MemberTransaction refTransaction,final Member partnerMember, Coin coin){
        //添加返佣记录
        RewardRecord rewardRecord = new RewardRecord();
        rewardRecord.setAmount(rewardAmount);
        rewardRecord.setCoin(coin);
        rewardRecord.setMember(partnerMember);
        rewardRecord.setRemark(rewardPromotionSetting.getType().getCnName());
        rewardRecord.setType(RewardRecordType.PARTNER); //合伙人推广返佣
        rewardRecord.setRefTransactionId(refTransaction.getId());  //关联的是 member_transaction 表中类型“3=EXCHANGE(币币交易)”对应的交易记录ID
        rewardRecord.setLevel(rewardRecordLevel);
        if(rewardRecordStatus == RewardRecordStatus.TREATED) {
            rewardRecord.setTreatedTime(new Date());
        } else {
            rewardRecord.setTreatedTime(null);
        }
        rewardRecord.setFromMemberId(refTransaction.getMemberId());
        rewardRecord.setFromAmount(rewardAmount);
        rewardRecord.setFromCoinUnit(coin.getUnit());
        rewardRecord.setExchangeRate(BigDecimal.valueOf(1L)); //未进行币种转化，所以汇率为1
        rewardRecord.setStatus(rewardRecordStatus);
        rewardRecord.setRewardCycle(rewardPromotionSetting.getRewardCycle());
        return rewardRecordService.save(rewardRecord);
    }

    private PartnerRewardService getService(){
        return SpringContextUtil.getBean(this.getClass());
    }
}
