package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockMarketRewardDetail;
import com.spark.bitrade.entity.LockMemberIncomePlan;
import com.spark.bitrade.vo.StoLockDepDetailVo;
import com.spark.bitrade.vo.StoLockDepVo;
import com.spark.bitrade.vo.StoLockIncomeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2018.12.03 20:12
 */
@Mapper
public interface LockMarketRewardDetailMapper {
    /**
     * 根据id查询
     */
    LockMarketRewardDetail findOneById(@Param("id") Long id);

    /**
     * 通过锁仓id和用户id查询奖励明细
     * @author tansitao
     * @time 2018/12/5 14:10 
     */
    LockMarketRewardDetail findOneByLockDetailAndMemberId(@Param("lockDetailId") Long lockDetailId, @Param("memberId") Long memberId);

    /**
     * 查询部门锁仓记录
     */
    List<StoLockDepDetailVo> findDepByInviter(@Param("memberId") Long memberId, @Param("symbol") String symbol,
                                              @Param("startTime") String startTime, @Param("endTime")String endTime);

    /**
     * 获取用户子部门锁仓汇总信息
     */
    List<StoLockDepVo> findDepByInviterAsTotal(@Param("memberId") Long memberId,@Param("symbol") String symbol,
                                               @Param("startTime") String startTime, @Param("endTime")String endTime);

    /**
     * 用户当前职务
     */
    String findLevelByMemberId(@Param("memberId") Long memberId);

    /**
     * 查询用户奖励收益记录
     */
    List<StoLockIncomeVo> queryMemberRewardIncome(@Param("memberId") Long memberId,@Param("symbol") String symbol,
                                                  @Param("startTime") String startTime, @Param("endTime") String endTime);
    List<StoLockIncomeVo> queryMemberRewardIncome(@Param("memberId") Long memberId,@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 查询用户一段时间內子部门业绩总和
     */
    BigDecimal findSubPerformanceAmountById(@Param("memberId") Long memberId,@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("symbol") String symbol);
}
