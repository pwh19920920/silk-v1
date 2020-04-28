package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.vo.JackpotStatisticsVo;
import com.spark.bitrade.vo.RewardStatisticsVo;
import com.spark.bitrade.vo.VoteStatisticsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 报表统计mapper
 * @author Zhang Yanjun
 * @time 2018.09.17 10:08
 */
@Mapper
public interface StatisticsMapper {
    //疯狂比特投票统计
     List<VoteStatisticsVo> voteStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

    //疯狂比特中奖统计
    List<RewardStatisticsVo> rewardStatistivs(@Param("startTime") String startTime, @Param("endTime") String endTime);

    //疯狂比特奖池统计
    List<JackpotStatisticsVo> jackpotStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

}
