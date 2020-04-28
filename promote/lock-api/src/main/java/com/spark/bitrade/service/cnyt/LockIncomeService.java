package com.spark.bitrade.service.cnyt;

import com.spark.bitrade.config.LockConfig;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.LockRewardSatus;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mq.CnytMessageType;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 处理锁仓收益服务实现类
 * @author fumy
 * @time 2018.12.05 15:10
 */
@Service
@Slf4j
public class LockIncomeService {

    @Autowired
    private InviterRewardService inviterRewardService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LockMemberIncomePlanService incomePlanService;

    @Autowired
    private LockMarketRewardDetailService rewardDetailService;

    @Autowired
    private LockMarketLevelService lockMarketLevelService;

    @Autowired
    private LockCoinActivitieSettingService settingService;

    @Autowired
    private LockMarketPerformanceTotalService performanceTotalService;

    @Autowired
    private SendMessageService sendMessageService;


    @Autowired
    private LockConfig lockConfig;

    @Autowired
    private UpdateLockUserLevelService updateLockUserLevelService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    /**
     * STO增值计划锁仓操作异步调起接口
     * @author fumy
     * @time 2018.12.05 18:15
     * @param lockCoinDetail
     * @return true
     */
    @Async
    public void dealIncomeMessage(LockCoinDetail lockCoinDetail){
        /**
         * 1,获取处理锁仓收益消息实体
         * 2,保存推荐人奖励明细记录
         * 3,更新个人锁仓数量
         * 4,生成收益返回计划记录
         * 5,推送直推奖励处理消息
         */
        //日志前缀
        String logPrefix = this.getLogPrefix(lockCoinDetail);

        //根据关联锁仓记录id查询锁仓记录
        log.info("{}===============处理锁仓收益消息：{}=====================" , logPrefix, lockCoinDetail);

        if(lockCoinDetail != null){
            LockMarketRewardDetail rewardDetail = getService().generateIncomePlan(lockCoinDetail,
                    incomePlanService.countWaitBack(lockCoinDetail.getId()),
                    memberService.findOne(lockCoinDetail.getMemberId()),
                    settingService.findOne(lockCoinDetail.getRefActivitieId()));
            if(rewardDetail != null){
                log.info("{}，RDId={}，发送处理直推奖励的通知", logPrefix , rewardDetail.getId());

                sendMessageService.sendCnytMessage(rewardDetail, rewardDetail, CnytMessageType.PUSH_REWARD, null);
            }else {
                getService().updateLockCoinDetailRewardSatus(lockCoinDetail);
                log.info("{}，用户{}的推荐人不存在，不处理直推奖", logPrefix, lockCoinDetail.getMemberId());
            }


            //add by yangch 时间： 2019.03.17 原因：异步更新锁仓用户的等级，满足“本人锁仓5万360天”条件可升级为“初级节点”的需求
            updateLockUserLevelService.updateLockUserLevel(lockCoinDetail.getMemberId(), lockCoinDetail.getCoinUnit());
        }else {
            log.warn("============锁仓记录不存在：{}==============");
        }

        log.info("{}，===============处理锁仓收益消息结束=====================" , logPrefix);
    }

    /**
     *
     * @param lockCoinDetail
     * @param incomePlansNum
     * @param inviteeMember
     * @param activitieSetting
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public LockMarketRewardDetail generateIncomePlan(LockCoinDetail lockCoinDetail,
                                                     int incomePlansNum, Member inviteeMember, LockCoinActivitieSetting activitieSetting){
        //日志前缀
        String logPrefix = this.getLogPrefix(lockCoinDetail);
        log.info("{}，生成用户{}的锁仓收益计划", logPrefix, lockCoinDetail.getMemberId());

        //查询活动获取锁仓记录的奖励奖励系数
        lockCoinDetail.setLockDays(activitieSetting.getLockDays());
        //生成锁仓收益计划
        //由于考虑数据重做，生成锁仓收益列表时，先统计是否存在收益列表数据
        //不存在,生成新的返还计划列表
        if(incomePlansNum == 0) {
            //创建收益计划列表
            getService().createIncomeList(lockCoinDetail, logPrefix);
        }

        //更新个人锁仓数量、保存用户奖励明细记录
        log.info("{}，预处理直推奖励明细记录，memberId={}", logPrefix, lockCoinDetail.getMemberId());


        if(StringUtils.isEmpty(inviteeMember.getInviterId()) == false) {
            LockMarketRewardDetail lockMarketRewardDetail =
                    rewardDetailService.findOneByLockDetailAndMember(lockCoinDetail.getId(), inviteeMember.getInviterId());
            //判断是否存在邀请人的奖励明细记录
            if(lockMarketRewardDetail !=null){
                return lockMarketRewardDetail;
            } else {
                if (lockCoinDetail.getMemberId() == inviteeMember.getInviterId().longValue()) {
                    log.info("{}，推荐人不能为自己，退出直推奖励、市场奖励的处理，inviteeId={}，inviterId={}"
                            , logPrefix, lockCoinDetail.getMemberId(), inviteeMember.getInviterId());
                } else {
                    Member inviterMember = memberService.findOne(inviteeMember.getInviterId());
                    if (StringUtils.isEmpty(inviterMember)) {
                        log.warn("{}，用户{}推荐用户{}不存在--------", logPrefix, inviteeMember, inviteeMember.getInviterId());
                    } else {
                        //更新个人锁仓数量
                        log.info("{}，更新用户{}个人锁仓数量--------", logPrefix, lockCoinDetail.getMemberId());
                        getService().saveOwnLockAmountTotal(lockCoinDetail, inviterMember);


                        //查询个人等级，无则初始化个人等级
                        ///LockMarketLevel  lockMarketLevel = levelService.findByMemberId(lockCoinDetail.getMemberId(), lockCoinDetail.getCoinUnit());

                        //保存用户奖励明细记录
                        //获取推荐用户等级
                        LockMarketLevel inviterLevel = lockMarketLevelService.findByMemberId(inviteeMember.getInviterId(), lockCoinDetail.getCoinUnit());

                        log.info("{}，预处理直推奖励明细记录，inviteeId={}，inviterId={}"
                                , logPrefix, lockCoinDetail.getMemberId(), inviterMember.getId());
                        //转换奖励明细对象并保存
                        lockMarketRewardDetail =
                                inviterRewardService.getPreDirectInviterLockMarketRewardDetail(lockCoinDetail, null,
                                        inviterMember, inviterLevel, activitieSetting.getRewardFactor());

                        return rewardDetailService.save(lockMarketRewardDetail);
                    }
                }
            }
        } else {
            log.info("{}，用户{}的推荐人不存在，退出直推奖励、市场奖励的处理"
                    , logPrefix, lockCoinDetail.getMemberId());
        }

        return null;
    }

    /**
     * 更新个人锁仓数量
     * @param lockCoinDetail
     * @param inviterMember
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOwnLockAmountTotal(LockCoinDetail lockCoinDetail, Member inviterMember) {
        //查询用户是否存在个人业绩
        LockMarketPerformanceTotal performanceTotal =
                performanceTotalService.findByMemberId(lockCoinDetail.getMemberId(), lockCoinDetail.getCoinUnit());
        if (performanceTotal == null) {
            //没有查询到个人总业绩记录，创建新的个人业绩记录
            performanceTotal = new LockMarketPerformanceTotal();
            performanceTotal.setMemberId(lockCoinDetail.getMemberId());
            performanceTotal.setOwnLockAmountTotal(lockCoinDetail.getTotalAmount());
            performanceTotal.setSubDepartmentAmountTotal(BigDecimal.ZERO);
            performanceTotal.setIniviteId(inviterMember.getId());
            performanceTotal.setCreateTime(new Date());
            performanceTotal.setSymbol(lockCoinDetail.getCoinUnit());

        } else {
            performanceTotal.setOwnLockAmountTotal(performanceTotal.getOwnLockAmountTotal().add(lockCoinDetail.getTotalAmount()));
            performanceTotal.setUpdateTime(new Date());
        }
        performanceTotalService.save(performanceTotal);
    }

    /**
     * 创建收益计划列表
     * @param lockCoinDetail
     * @param logPrefix
     */
    @Transactional(rollbackFor = Exception.class)
    public void createIncomeList(LockCoinDetail lockCoinDetail,String logPrefix) {
        //计算收益分期数
        int periods = lockCoinDetail.getLockDays() / lockConfig.getCycle(lockCoinDetail.getCoinUnit());
        if (periods > 0) {
            //计算单期收益
            BigDecimal singlePeroidIncome = lockCoinDetail.getPlanIncome().divide(BigDecimal.valueOf(periods), 8, BigDecimal.ROUND_DOWN);
            for (int i = 1; i <= periods; i++) {
                LockMemberIncomePlan incomePlan = new LockMemberIncomePlan();
                incomePlan.setMemberId(lockCoinDetail.getMemberId());
                //如果是最后一期（将分期舍掉的部分放到最后一期一起返还） = 用总的收益 - 前几期的收益之和
                if (i == periods && i > 1) {
                    incomePlan.setAmount(lockCoinDetail.getPlanIncome().subtract(singlePeroidIncome.multiply(BigDecimal.valueOf(i - 1))));
                } else {
                    incomePlan.setAmount(singlePeroidIncome);
                }
                incomePlan.setPeriod(i);
                incomePlan.setLockDetailId(lockCoinDetail.getId());
                incomePlan.setRewardTime(DateUtil.addDay(lockCoinDetail.getLockTime(), i * lockConfig.getCycle(lockCoinDetail.getCoinUnit())));
                incomePlan.setStatus(LockBackStatus.BACK);
                incomePlan.setCreateTime(new Date());
                incomePlan.setSymbol(lockCoinDetail.getCoinUnit());
                incomePlanService.save(incomePlan);
            }
        } else {
            log.warn("{}，计算收益分期数小于0，LockDays={}，PERIOD={}"
                    , logPrefix, lockCoinDetail.getLockDays(), lockConfig.getCycle(lockCoinDetail.getCoinUnit()));
        }
    }

    /**
     * 处理完推荐关系后，更新LockCoinDetail.lockRewardSatus=ALREADY_REWARD(已返佣)
     *
     * @param lockCoinDetail
     */
    public void updateLockCoinDetailRewardSatus(LockCoinDetail lockCoinDetail) {
        lockCoinDetailService.updateLockRewardSatus(lockCoinDetail.getId(),
                LockRewardSatus.NO_REWARD, LockRewardSatus.ALREADY_REWARD);
    }

    public LockIncomeService getService(){
        return SpringContextUtil.getBean(LockIncomeService.class);
    }

    private String getLogPrefix(LockCoinDetail lockCoinDetail){
        return String.format("LDId=%d,memberId=%d ", lockCoinDetail.getId(), lockCoinDetail.getMemberId());
    }

}
