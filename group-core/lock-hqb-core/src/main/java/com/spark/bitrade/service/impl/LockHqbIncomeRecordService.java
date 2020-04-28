package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.LockHqbIncomeRecord;
import com.spark.bitrade.mapper.dao.LockHqbIncomeRecordMapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LockHqbIncomeRecordService extends ServiceImpl<LockHqbIncomeRecordMapper, LockHqbIncomeRecord> implements ILockHqbIncomeRecordService {

    @Autowired
    private LockHqbIncomeRecordMapper lockHqbIncomeRecordMapper;


    @Override
    public List<LockHqbIncomeRecord> findByMemberIdAndAppIdAndUnitLimitBy(Long memberId, String appId, String unit, Integer limit) {
        return lockHqbIncomeRecordMapper.findByMemberIdAndAppIdAndUnitLimitBy(memberId,appId,unit,limit);
    }
}
