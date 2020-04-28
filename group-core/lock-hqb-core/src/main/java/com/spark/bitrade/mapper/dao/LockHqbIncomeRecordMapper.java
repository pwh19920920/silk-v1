package com.spark.bitrade.mapper.dao;


import com.spark.bitrade.entity.LockHqbIncomeRecord;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LockHqbIncomeRecordMapper extends BaseMapper<LockHqbIncomeRecord> {

    List<LockHqbIncomeRecord> findByMemberIdAndAppIdAndUnitLimitBy(@Param("memberId") Long memberId, @Param("appId") String appId, @Param("unit") String unit, @Param("limit") Integer limit);

}
