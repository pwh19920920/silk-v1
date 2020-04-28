package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockMemberIncomePlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2018.12.03 20:12
 */
@Mapper
public interface LockMemberIncomePlanMapper {
    /**
     * 待返还列表
     */

    List<LockMemberIncomePlan> findAllByBack();

    /**
     * 根据id查询
     */
    LockMemberIncomePlan findOneById(@Param("id") Long id);

    /**
     * 根据锁仓记录id统计待返还的收益期数
     */
    int countWaitBackByLockDetailId(@Param("lockCoinDetailId") Long lockCoinDetailId);
}
