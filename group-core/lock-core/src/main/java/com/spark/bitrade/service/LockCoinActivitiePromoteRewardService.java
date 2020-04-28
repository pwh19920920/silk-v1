package com.spark.bitrade.service;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

/***
 * 锁仓活动返佣接口
 * @author yangch
 * @time 2018.07.25 14:27
 */
@Slf4j
@Service
public class LockCoinActivitiePromoteRewardService {
    @Autowired
    LockCoinDetailService lockCoinDetailService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private GyDmcodeService gyDmcodeService;
    @Autowired
    private PartnerAreaService partnerAreaService;
    @Autowired
    private CoinService coinService;


    /**
     *  SLB节点产品（量化基金） 推荐返佣
     * @param member
     * @param lockCoinActivitieSetting
     * @param lockCoinDetail
     */
    @Transactional(rollbackFor = Exception.class)
    public void promotionReward4joinQuantifyLock(final Member member, final LockCoinActivitieSetting lockCoinActivitieSetting , final LockCoinDetail lockCoinDetail) throws Exception{
        //SLB节点产品（量化基金） 推荐奖励
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.ACTIVE_QUANTIFY);
        if (rewardPromotionSetting != null) {
            if (rewardPromotionSetting.getEffectiveTime() ==0 ||
                    (DateUtil.diffDays(new Date(), rewardPromotionSetting.getUpdateTime()) <= rewardPromotionSetting.getEffectiveTime())) {

                JSONObject jsonObject = JSONObject.parseObject(rewardPromotionSetting.getInfo());
                if(null == jsonObject){
                    log.warn("未能正确解析SLB节点产品返佣配置规则");
                    lockRewardSatus4Success(lockCoinDetail.getId());
                    return;
                }

                //推荐人
                Member presenter = null;
                //判断用户是否为合伙下的用户
                if( StringUtils.hasText(member.getAreaId()) ) {
                    String areaId = member.getAreaId().substring(0,2);
                    DimArea dimArea = gyDmcodeService.findOneByDmCode(areaId);
                    if(dimArea != null) {
                        PartnerArea partnerArea = partnerAreaService.findPartnerAreaByAreaId(dimArea);
                        if (null != partnerArea && partnerArea.getPartnerStaus()==PartnerStaus.normal
                                && partnerArea.getLevel() == PartnerLevle.M1) {
                            //判断是否为 M1用户
                            presenter = partnerArea.getMember();
                        } else {
                            log.info("没有获取到{}用户的合伙人", member.getId());
                        }
                    } else {
                        log.info("{}用户的区域不存在", member.getId());
                    }
                } else {
                    log.info("该账号没有归属区域，帐号={}", member.getId());
                }


                if(presenter==null) {
//                    if(member.getInviterId() == null) {
//                        log.info("该账号没有推荐人，帐号={}", member.getId());
//                        lockRewardSatus4Success(lockCoinDetail.getId());
//                        return;
//                    }
                    presenter = lockCoinDetailService.getRewardMember(member.getInviterId());
                    if (null == presenter) {
                        log.info("没有找到{}帐号的推荐用户 或 推荐用户不具备返佣资格", member.getId());
                        lockRewardSatus4Success(lockCoinDetail.getId());
                        return;
                    }
                    if (presenter.getStatus() == CommonStatus.ILLEGAL) {
                        log.info("{}帐号已经被禁用，不发放SLB节点产品佣金", presenter.getId());
                        lockRewardSatus4Success(lockCoinDetail.getId());
                        return;
                    }

//                    if (presenter.getRegistrationTime().getTime() >= lockCoinActivitieSetting.getStartTime().getTime()
//                            && presenter.getInviterId() == null) {
//                        log.info("【SLB节点产品】-------该用户的推荐人(ID={})为SLB节点产品开始后注册的新用户，且没有推荐人，不具备共识奖励的资格.....购买用户ID={}", presenter.getId(), member.getId());
//                        lockRewardSatus4Success(lockCoinDetail.getId());
//                        return;
//                    }
                }

                //返佣人不能为自己
//                if(member.getId().compareTo(presenter.getId())==0){
//                    log.warn("返佣人不能为自己,购买用户ID={}", member.getId());
//                    lockRewardSatus4Success(lockCoinDetail.getId());
//                    return;
//                }

                //总投资的人民币数量
                BigDecimal totalCNRAmount = lockCoinDetailService.getUserQuantifyTotalCny(presenter.getId());
                if(totalCNRAmount==null){
                    totalCNRAmount = BigDecimal.ZERO;
                }

                //返佣比例
                BigDecimal rewardRate = BigDecimal.ZERO ;
                //初级节点
                if( totalCNRAmount.compareTo(BigDecimal.valueOf(10000L)) >=0 ) {
                    rewardRate = jsonObject.getBigDecimal("one");
                    if (rewardRate == null || rewardRate.compareTo(BigDecimal.ZERO) <= 0) {
                        log.info("没有配置初级节点推荐返佣");
                        lockRewardSatus4Success(lockCoinDetail.getId());
                        return;
                    }
                }
//                else {
//                    //老用户
//                    if(presenter.getRegistrationTime().getTime() < lockCoinActivitieSetting.getStartTime().getTime()) {
//                        //判断老用户的SLB数量是否大于10000
//                        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol(), presenter.getId());
//                        if (memberWallet == null) {
//                            log.warn("推荐用户钱包账户为空，ID={}", presenter.getId());
//                            lockRewardSatus4Success(lockCoinDetail.getId());
//                            return;
//                        }
//
//                        BigDecimal totalBalance = memberWallet.getBalance().add(memberWallet.getFrozenBalance()).add(memberWallet.getLockBalance());
//                        if (totalBalance.compareTo(BigDecimal.valueOf(10000L)) >= 0) {
//                            rewardRate = jsonObject.getBigDecimal("one");
//                            if (rewardRate == null || rewardRate.compareTo(BigDecimal.ZERO) <= 0) {
//                                log.info("没有配置初级节点推荐返佣");
//                                lockRewardSatus4Success(lockCoinDetail.getId());
//                                return;
//                            }
//                        }
//                    }
//                }
                //高级节点
                if( totalCNRAmount.compareTo(BigDecimal.valueOf(100000L)) >=0 ) {
                    rewardRate = jsonObject.getBigDecimal("two");
                    if (rewardRate == null || rewardRate.compareTo(BigDecimal.ZERO) <= 0) {
                        log.info("没有配置高级节点推荐返佣");
                        lockRewardSatus4Success(lockCoinDetail.getId());
                        return;
                    }
                }
                //超级节点
                if( totalCNRAmount.compareTo(BigDecimal.valueOf(500000L)) >=0 ) {
                    rewardRate = jsonObject.getBigDecimal("three");
                    if (rewardRate == null || rewardRate.compareTo(BigDecimal.ZERO) <= 0) {
                        log.info("没有配置超级节点推荐返佣");
                        lockRewardSatus4Success(lockCoinDetail.getId());
                        return;
                    }
                }

                if(rewardRate.compareTo(BigDecimal.ZERO)==0){
                    log.info("该用户的推荐人没有达到SLB节点产品返佣条件，用户ID={},推荐人ID={}", member.getId(), presenter.getId());
                    lockRewardSatus4Success(lockCoinDetail.getId());
                    return;
                }

                if(lockCoinActivitieSetting.getLockDays() == 180){
                    rewardRate = rewardRate.divide(new BigDecimal(2));
                    log.info("该用户购买的为半年周期的SLB节点产品，共识奖励减半,用户ID={},减半后的奖励比例为{}",member.getId(),rewardRate);
                }
                //如果是福建合伙人，则按超级节点比例返佣
                if(presenter.getId()==100529L){
                    rewardRate = jsonObject.getBigDecimal("three");
                    log.info("该用户为福建合伙人，用户ID={},最终奖励比例为{}",member.getId(),rewardRate);
                }


                //计算返佣金额
                //返佣到USDT帐号
                String incomeSymbol = "USDT";
                //USDT数量
                BigDecimal totalUSDTAmount = lockCoinDetail.getLockPrice().multiply(lockCoinDetail.getTotalAmount()).setScale(8,BigDecimal.ROUND_DOWN);
                //返佣金额
                BigDecimal rewardAmount = BigDecimalUtils.mulRound(totalUSDTAmount, BigDecimalUtils.getRate(rewardRate), 8);

                //返佣账户
                MemberWallet promoteMemberWalletCeche = memberWalletService.findCacheByCoinUnitAndMemberId(incomeSymbol, presenter.getId());

                //添加返佣记录
                RewardRecord rewardRecord = new RewardRecord();
                rewardRecord.setAmount(rewardAmount);
                rewardRecord.setCoin(promoteMemberWalletCeche.getCoin());
                rewardRecord.setMember(presenter);
                rewardRecord.setRemark(rewardPromotionSetting.getType().getCnName());
                rewardRecord.setType(RewardRecordType.ACTIVITY);
                //关联的是 lock_coin_detail 表中类型“20=QUANTIFY_ACTIVITY”对应的交易记录ID
                rewardRecord.setRefTransactionId(lockCoinDetail.getId());
                rewardRecord.setLevel(RewardRecordLevel.ONE);
                rewardRecord.setTreatedTime(new Date());
                rewardRecord.setFromMemberId(member.getId());
                rewardRecord.setFromAmount(rewardAmount);
                rewardRecord.setFromCoinUnit(promoteMemberWalletCeche.getCoin().getUnit());
                //未进行币种转化，所以汇率为1
                rewardRecord.setExchangeRate(BigDecimal.valueOf(1L));
                rewardRecord.setRewardCycle(rewardPromotionSetting.getRewardCycle());
                //已发放
                rewardRecord.setStatus(RewardRecordStatus.TREATED);
                RewardRecord rewardRecordNew = rewardRecordService.save(rewardRecord);

                MessageResult result = memberWalletService.increaseBalance(promoteMemberWalletCeche.getId(),rewardAmount);
                //返佣成功，添加交易记录
                if(result.getCode()==0) {
                    //再添加交易记录
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setAmount(rewardAmount);
                    memberTransaction.setFee(BigDecimal.ZERO);
                    memberTransaction.setFeeDiscount(BigDecimal.ZERO);
                    memberTransaction.setMemberId(presenter.getId());
                    memberTransaction.setSymbol(incomeSymbol);
                    //交易记录为“20=QUANTIFY_ACTIVITY(量化投资)”
                    memberTransaction.setType(TransactionType.LOCK_COIN_PROMOTION_AWARD);
                    //关联“reward_record”表中对应的返佣记录
                    memberTransaction.setRefId(String.valueOf(rewardRecordNew.getId()));
                    transactionService.save(memberTransaction);
                }

                //更新返佣状态
                //lockCoinDetail.setLockRewardSatus(LockRewardSatus.ALREADY_REWARD); //已返佣
                //lockCoinDetailService.save(lockCoinDetail);
               int refCode = lockCoinDetailService.updateLockRewardSatus(lockCoinDetail.getId(),LockRewardSatus.NO_REWARD, LockRewardSatus.ALREADY_REWARD);
               if(refCode==0){
                   log.error("修改活动记录的返佣状态失败，活动ID={}", lockCoinDetail.getId());
                   throw new UnexpectedException("修改返佣状态失败");
               }
            } else {
                log.warn("SLB节点产品（量化基金）推荐返佣活动已过期");
                lockRewardSatus4Success(lockCoinDetail.getId());
            }
        } else {
            log.warn("没有获取到SLB节点产品（量化基金）推荐返佣配置,配置={},用户id={}",rewardPromotionSetting, member.getId());
            lockRewardSatus4Success(lockCoinDetail.getId());
        }
    }
    //返佣成功
    private void lockRewardSatus4Success(Long id){
        if(id!=null) {
            lockCoinDetailService.updateLockRewardSatus(id, LockRewardSatus.NO_REWARD, LockRewardSatus.ALREADY_REWARD);
        }
    }


    /**
     * STO锁仓 推荐奖励 处理
     * @param member 参与活动的用户
     * @param lockCoinActivitieSetting 活动配置信息
     * @param lockCoinDetail 参与的活动记录
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void promotionReward4StoLock(final Member member, final LockCoinActivitieSetting lockCoinActivitieSetting
            , final LockCoinDetail lockCoinDetail) throws Exception{
        //STO锁仓 推荐奖励
        if(StringUtils.isEmpty(member.getInviterId())){
            log.warn("{}用户无推荐关系，参与的活动记录id={}", member.getId(), lockCoinDetail.getId());
            lockRewardSatus4Success(lockCoinDetail.getId());
            return;
        }

        if(lockCoinDetail.getType() != LockType.STO ) {
            log.warn("不是STO锁仓活动，参与的活动记录id={}", lockCoinDetail.getId());
            return;
        }

        //获取活动配置的币种信息
        Coin coin = coinService.findByUnit(lockCoinActivitieSetting.getCoinSymbol());
        //获取返佣配置(这里只满足多币种锁仓，只根据类型和币种单位查询每个类型每个币种组成唯一的返佣配置)
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByTypeAndCoin(PromotionRewardType.ACTIVE_STO,coin);
        if (rewardPromotionSetting != null) {
            if (rewardPromotionSetting.getEffectiveTime() ==0 ||
                    (DateUtil.diffDays(new Date(), rewardPromotionSetting.getUpdateTime()) <= rewardPromotionSetting.getEffectiveTime())) {
                //返佣的计算金额
                BigDecimal incomeAmount = lockCoinDetail.getPlanIncome();

                //返佣配置
                JSONObject jsonObject = JSONObject.parseObject(rewardPromotionSetting.getInfo());
                if(null == jsonObject){
                    log.warn("未能正确解析STO佣配置规则");
                    lockRewardSatus4Success(lockCoinDetail.getId());
                    return;
                }

                //一级返佣
                BigDecimal rewardRate1 = jsonObject.getBigDecimal("one");
                Member member1 = promoteReward4Sto(lockCoinDetail, member, incomeAmount, rewardRate1, rewardPromotionSetting, RewardRecordLevel.ONE);

                //二级返佣
                BigDecimal rewardRate2 = jsonObject.getBigDecimal("two");
                Member member2 = promoteReward4Sto(lockCoinDetail, member1, incomeAmount, rewardRate2, rewardPromotionSetting, RewardRecordLevel.TWO);

                //三级返佣
                BigDecimal rewardRate3 = jsonObject.getBigDecimal("three");
                promoteReward4Sto(lockCoinDetail, member2, incomeAmount, rewardRate3, rewardPromotionSetting, RewardRecordLevel.THREE);

                //修改记录的返佣状态
                int refCode = lockCoinDetailService.updateLockRewardSatus(lockCoinDetail.getId(), LockRewardSatus.NO_REWARD, LockRewardSatus.ALREADY_REWARD);
                if(refCode==0){
                    log.error("修改活动记录的返佣状态失败，活动ID={}", lockCoinDetail.getId());
                    throw new UnexpectedException("修改返佣状态失败");
                }
            } else {
                log.warn("STO推荐返佣活动已过期");
                lockRewardSatus4Success(lockCoinDetail.getId());
            }
        } else {
            log.warn("没有获取到STO推荐推荐返佣配置,配置={},用户id={}",rewardPromotionSetting, member.getId());
            lockRewardSatus4Success(lockCoinDetail.getId());
        }
    }

    /**
     * 处理推荐关系
     * @param lockCoinDetail 锁仓活动记录
     * @param member 用户
     * @param incomeAmount 返佣的计算金额
     * @param rewardRate   返佣比例
     * @param rewardPromotionSetting 返佣配置
     * @param rewardRecordLevel 返佣级别
     * @return 返回推荐的用户
     */
    public Member promoteReward4Sto(final LockCoinDetail lockCoinDetail,final Member member ,
                              BigDecimal incomeAmount, final BigDecimal rewardRate,
                              final RewardPromotionSetting rewardPromotionSetting, final RewardRecordLevel rewardRecordLevel){
        if(member != null){
            //返佣
            if (StringUtils.isEmpty(member.getInviterId())) {
                log.info("没有找到{}帐号的推荐用户，参与的活动记录id={}", member.getId(), lockCoinDetail.getId());
                return null;
            }

            if (StringUtils.isEmpty(rewardRate) || rewardRate.compareTo(BigDecimal.ZERO) <= 0) {
                log.info("没有配置{}级推荐返佣", rewardRecordLevel.getCnName());
                return null;
            }

            Member promoteMember = memberService.findOne(member.getInviterId());
            if (promoteMember == null) {
                log.info("没有找到{}帐号的推荐用户，参与的活动记录id={}", member.getId(), lockCoinDetail.getId());
                return promoteMember;
            }
            if (promoteMember.getStatus() == CommonStatus.ILLEGAL) {
                log.info("{}帐号已经被禁用", promoteMember.getId());
            } else {
                BigDecimal rewardAmount = BigDecimalUtils.mulRound(incomeAmount, BigDecimalUtils.getRate(rewardRate), 8);
                savePromoteReward4Sto(lockCoinDetail, promoteMember , rewardAmount, rewardPromotionSetting, rewardRecordLevel);
            }
            return promoteMember;
        }

        return null;
    }

    /** 
      * @author yangch
      * @time 
     *  @param lockCoinDetail    锁仓活动记录
     *  @param promoteMember 返佣用户
     *   @param rewardAmount 返佣金额
     *   @param rewardPromotionSetting 返佣配置
     *   @param rewardRecordLevel 返佣级别
     * @return 
     */
    public void savePromoteReward4Sto(final LockCoinDetail lockCoinDetail,Member promoteMember ,
                                  BigDecimal rewardAmount, RewardPromotionSetting rewardPromotionSetting, RewardRecordLevel rewardRecordLevel){
        if (rewardAmount.compareTo(BigDecimal.ZERO) > 0 && rewardPromotionSetting!=null) {
            String incomeSymbol = lockCoinDetail.getCoinUnit();
            MemberWallet promoteMemberWalletCeche = memberWalletService.findCacheByCoinUnitAndMemberId(incomeSymbol, promoteMember.getId());
            if(promoteMemberWalletCeche == null) {
                promoteMemberWalletCeche = memberWalletService.createMemberWallet(promoteMember.getId(), coinService.findByUnit(lockCoinDetail.getCoinUnit()));
            }

            //添加返佣
            MessageResult result = memberWalletService.increaseBalance(promoteMemberWalletCeche.getId(),rewardAmount);

            //先添加返佣记录
            RewardRecord rewardRecord = new RewardRecord();
            rewardRecord.setAmount(rewardAmount);
            rewardRecord.setCoin(promoteMemberWalletCeche.getCoin());
            rewardRecord.setMember(promoteMember);
            rewardRecord.setRemark(rewardPromotionSetting.getType().getCnName());
            rewardRecord.setType(RewardRecordType.ACTIVITY);
            //关联的是 member_transaction 表中类型
            rewardRecord.setRefTransactionId(lockCoinDetail.getId());
            rewardRecord.setLevel(rewardRecordLevel);
            rewardRecord.setTreatedTime(new Date());
            rewardRecord.setFromMemberId(lockCoinDetail.getMemberId());
            rewardRecord.setFromAmount(rewardAmount);
            rewardRecord.setFromCoinUnit(promoteMemberWalletCeche.getCoin().getUnit());
            //未进行币种转化，所以汇率为1
            rewardRecord.setExchangeRate(BigDecimal.valueOf(1L));
            rewardRecord.setRewardCycle(rewardPromotionSetting.getRewardCycle());
            //返佣成功，标记为已发放
            if(result.getCode()==0) {
                //已发放
                rewardRecord.setStatus(RewardRecordStatus.TREATED);
            } else { //返佣失败，标记为 未发放
                //未发放
                rewardRecord.setStatus(RewardRecordStatus.UNTREATED);
            }
            RewardRecord rewardRecordNew = rewardRecordService.save(rewardRecord);

            //返佣成功，添加记录
            if(result.getCode()==0) {
                //再添加交易记录
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(rewardAmount);
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction.setMemberId(promoteMember.getId());
                memberTransaction.setSymbol(incomeSymbol);
                //交易记录为“22=LOCK_COIN_PROMOTION_AWARD_STO(STO推荐奖励)”
                memberTransaction.setType(TransactionType.LOCK_COIN_PROMOTION_AWARD_STO);
                //关联“reward_record”表中对应的返佣记录
                memberTransaction.setRefId(String.valueOf(rewardRecordNew.getId()));
                transactionService.save(memberTransaction);
            }
        } else{
            log.warn("返佣数量小于0或者没有3级返佣规则配置");
        }
    }



}
