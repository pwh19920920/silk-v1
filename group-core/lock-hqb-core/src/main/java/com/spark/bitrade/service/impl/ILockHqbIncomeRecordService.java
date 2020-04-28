package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.LockHqbIncomeRecord;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.04.23 14:24
 */
public interface ILockHqbIncomeRecordService extends IService<LockHqbIncomeRecord> {

    /**
     * 查询历史收益
     * @author Zhang Yanjun
     * @time 2019.04.24 11:23
      * @param memberId
     * @param appId
     * @param unit
     * @param limit
     */
    List<LockHqbIncomeRecord> findByMemberIdAndAppIdAndUnitLimitBy(Long memberId, String appId, String unit, Integer limit);
}
