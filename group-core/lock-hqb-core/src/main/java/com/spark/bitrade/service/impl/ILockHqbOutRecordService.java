package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.LockHqbOutRecord;
import com.baomidou.mybatisplus.service.IService;

import java.math.BigDecimal;

/**
 * @author Zhang Yanjun
 * @time 2019.04.23 14:52
 */
public interface ILockHqbOutRecordService extends IService<LockHqbOutRecord>{

    /**
     * 活期宝账户转出操作
     * @param memberId
     * @param appId
     * @param symbol
     * @param amount
     */
    void hqbTransferOutOperation(Long memberId, String appId, String symbol, BigDecimal amount);
}
