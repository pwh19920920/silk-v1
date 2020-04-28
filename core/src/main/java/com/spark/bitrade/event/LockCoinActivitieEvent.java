package com.spark.bitrade.event;

import com.spark.bitrade.constant.LockRewardSatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.entity.LockCoinActivitieSetting;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/***
 * 锁仓活动事件
 * @author yangch
 * @time 2018.07.25 14:01
 */

@Service
@Slf4j
public class LockCoinActivitieEvent {

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private MemberService memberService;
    @Autowired
    private LockCoinActivitiePromoteRewardService  activitiePromoteRewardService;

    /**
     * 异步处理锁仓活动奖励
     * @param
     */
    @Async
    public void ansyActivityPromotionReward1(LockCoinDetail lockCoinDetail,LockCoinActivitieSetting lockCoinActivitieSetting) {
        if(null == lockCoinDetail) {
            log.info("锁仓记录不存在");
            return;
        }

        Member member = memberService.findOne(lockCoinDetail.getMemberId());
        if (null == member) {
            log.info("锁仓记录的会员不存在，会员ID={}", lockCoinDetail.getMemberId());
            return;
        }

        try {
            activitiePromoteRewardService.promotionReward4joinQuantifyLock(member, lockCoinActivitieSetting, lockCoinDetail);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    /**
     * 异步处理锁仓STO活动奖励
     * @param
     */
    @Async
    public void ansyActivityPromotionReward4Sto(LockCoinDetail lockCoinDetail,LockCoinActivitieSetting lockCoinActivitieSetting) {
        if(null == lockCoinDetail) {
            log.info("锁仓记录不存在");
            return;
        }

        Member member = memberService.findOne(lockCoinDetail.getMemberId());
        if (null == member) {
            log.info("锁仓记录的会员不存在，会员ID={}", lockCoinDetail.getMemberId());
            return;
        }

        try {
            activitiePromoteRewardService.promotionReward4StoLock(member, lockCoinActivitieSetting, lockCoinDetail);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
