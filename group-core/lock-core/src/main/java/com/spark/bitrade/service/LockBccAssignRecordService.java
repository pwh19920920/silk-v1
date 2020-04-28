package com.spark.bitrade.service;


import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.LockBccAssignRecord;
import com.spark.bitrade.entity.LockCoinActivitieSetting;
import com.spark.bitrade.entity.LockMarketExtraRewardConfig;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.vo.LockBccLockedInfoVo;

import java.math.BigDecimal;


/**
 * (LockBccAssignRecord)表服务接口
 *
 * @author fatKarin
 * @since 2019-03-19 15:40:21
 */

public interface LockBccAssignRecordService {

    /**
     * 锁仓
     *
     * @param setting
     * @param user
     * @param coinCnyPrice
     * @param coinUSDTPrice
     * @param usdtPrice
     * @param amount
     * @param portion
     */
    void bccLock(LockCoinActivitieSetting setting, Member user, BigDecimal coinCnyPrice,
                    BigDecimal coinUSDTPrice, BigDecimal usdtPrice, BigDecimal amount, Integer portion);

    /**
     * 查询用户BCC赋能计划锁仓信息
     * @param memberId
     * @param setting
     * @return
     */
    LockBccLockedInfoVo findLockBccLockedInfo(Long memberId, LockCoinActivitieSetting setting);
}