package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.Reward;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RewardMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Reward record);

    int insertSelective(Reward record);

    Reward selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Reward record);

    int updateByPrimaryKey(Reward record);

    /**
     * 通过中奖金额排序，查询某一期所有中奖记录
     * @author tansitao
     * @time 2018/9/15 15:36 
     */
    List<Reward> pageQueryByType(@Param("periodId")long periodId, @Param("type")int type);

    List<Reward> queryByPeriodIdAndType(@Param("periodId") Long periodId,@Param("type") Integer type,@Param("memberId") Long memberId);

    /**
     * 倒叙查询领奖和红包信息
     * @author tansitao
     * @time 2018/9/26 14:14 
     */
    List<Reward> pageQueryAll(@Param("periodId") Long periodId);

    /**
     * 通过用户Id分页查询中奖数据
     * @author tansitao
     * @time 2018/9/15 15:37 
     */
    List<Reward> pageQueryByMemberId(@Param("memberId")long memberId, @Param("type")int type);

    /**
      * 通过用户id和期数id查询中奖信息
      * @author tansitao
      * @time 2018/9/15 17:40 
      */
    List<Reward> findOneByMemberIdAndPeriodId(@Param("memberId")long memberId, @Param("periodId")long periodId, @Param("type") int type ,@Param("status") int status);

    /**
      * 通过投注id查询和中奖类型获取中奖信息
      * @author tansitao
      * @time 2018/9/15 17:40 
      */
    Reward findByBettingId(@Param("memberId")long memberId,@Param("bettingId") long bettingId,@Param("type") int type,@Param("status") int status);


    /**
     * 根据期数统计本期红包中奖总额
     * @param params
     * @return
     */
    BigDecimal findRewardAmount(Map<String,Object> params);

}