package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.entity.LockBttcRestitutionIncomePlan;
import com.spark.bitrade.entity.LockCoinDetail;

import java.util.Date;
import java.util.List;


public interface LockBttcRestitutionIncomePlanService extends IService<LockBttcRestitutionIncomePlan> {

    /**
     * BTTC收益返还记录查询
     *
     * @param lockDetailId 锁仓记录详情id
     * @author Zhang Yanjun
     * @time 2019.04.17 15:58
     */
    PageInfo<LockBttcRestitutionIncomePlan> findById(Long lockDetailId, Integer pageNo, Integer pageSize);

    /**
     * 查找bttc锁仓可以返还的记录
     * @param status
     * @param lockNum
     * @param rewardTime
     * @return
     */
    List<LockBttcRestitutionIncomePlan> getCanRestitutionList(LockBackStatus status, int lockNum, Date rewardTime);

    /**
     * 返还
     * @param id
     * @param oldStatus
     * @param newStatus
     * @return
     */
    boolean updateStatus(Long id, LockBackStatus oldStatus, LockBackStatus newStatus);

    //jpa
    LockBttcRestitutionIncomePlan findLastPlanByMemberIdAndDetailId(Long memberId,Long detailId);

    List<Long> findIeoUnlockDetailToday();


    String findInnerList();
}

