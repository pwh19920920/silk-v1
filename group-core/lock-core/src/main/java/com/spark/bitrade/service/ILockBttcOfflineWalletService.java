package com.spark.bitrade.service;

import com.spark.bitrade.vo.LockBttcOfflineWalletVo;

import java.math.BigDecimal;

public interface ILockBttcOfflineWalletService {

    /**
     * 查询账户信息
     * @param memberId
     * @return
     */
    LockBttcOfflineWalletVo findLockBttcOfflineWalletVoByMemberId(Long memberId);

    BigDecimal findLockBttcIeoOfflineWalletBalanceByMemberId(Long memberId);

    /**
     * 锁定bttc解锁金钥匙
     * @param memberId
     * @param lockAmount
     * @param activityId
     */
    void lockReleaseGoldenKey(Long memberId, BigDecimal lockAmount, Long activityId) throws Exception;

    Long lockReleaseGoldenKeyForIeo(Long memberId, BigDecimal lockAmount, Long activityId,BigDecimal changeAmount,BigDecimal unlockRate,boolean flag , BigDecimal reduceAmount,boolean doRecord,BigDecimal lastAmount) throws Exception;

}
