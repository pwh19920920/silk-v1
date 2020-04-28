package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.LockHqbInRecord;
import com.baomidou.mybatisplus.service.IService;

import java.math.BigDecimal;

/**
 * @author Zhang Yanjun
 * @time 2019.04.23 16:24
 */
public interface ILockHqbInRecordService extends IService<LockHqbInRecord>{

    /**
     * 活期宝账户转入操作
     * @param memberId
     * @param appId
     * @param symbol
     * @param amount
     */
    void hqbTransferInOperation(Long memberId, String appId, String symbol, BigDecimal amount);


}
