package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockMarketLevel;
import com.spark.bitrade.entity.LockMarketPerformanceTotal;
import com.spark.bitrade.vo.StoLockDepVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员市场奖励业绩总累计Mapper
 * @author Zhang Yanjun
 * @time 2018.12.03 20:01
 */
@Mapper
public interface LockMarketPerformanceTotalMapper {
    /**
     * 根据会员id查询
     */
    LockMarketPerformanceTotal findByMemberId(@Param("memberId") Long memberId, @Param("symbol") String symbol);

    /**
     * 查询子部门列表
     */
    List<LockMarketPerformanceTotal> findAllByInivite(@Param("memebrId") Long memebrId, @Param("symbol") String symbol);

    /**
     *用户子部门锁仓汇总信息
     */
    List<StoLockDepVo> findTotalByInivite(@Param("memberId") Long memberId, @Param("symbol") String symbol, @Param("startTime") String startTime, @Param("endTime")String endTime);
}
