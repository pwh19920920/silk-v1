package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockMarketRewardIncomePlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Zhang Yanjun
 * @time 2018.12.03 20:12
 */
@Mapper
public interface LockMarketRewardIncomePlanMapper {
    /**
     * 待返还列表
     */

    List<LockMarketRewardIncomePlan> findAllByBack();

    /**
     * 根据id查询
     */
    LockMarketRewardIncomePlan findOneById(@Param("id") Long id);

    /**
     * 用户到账奖励
     */
    List<Map<String,Object>> findAllByBacked(@Param("memberId") Long memberId, @Param("symbol") String symbol,
                                             @Param("startTime") String startTime, @Param("endTime")String endTime);

    /**
     * 用户总奖励
     */
    List<Map<String,Object>> findAllReward(@Param("memberId") Long memberId,@Param("symbol") String symbol,
                                           @Param("startTime") String startTime, @Param("endTime")String endTime);

    /**
     * 查询一条满足返佣的数据
     */
    LockMarketRewardIncomePlan findOneByDetailIdAndMemberId(@Param("marketRewardDetailId") Long marketRewardDetailId, @Param("memberId") Long memberId, @Param("rewardTime")String rewardTime, @Param("status")int status);

    /**
     * 查询满足解锁的推荐人返佣记录
     * @author tansitao
     * @time 2018/12/28 16:44 
     */
    List<LockMarketRewardIncomePlan> findCanUnLockList(@Param("status")int status, @Param("lockNum")int lockNum, @Param("rewardTime")Date rewardTime);

}
