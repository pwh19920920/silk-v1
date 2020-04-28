package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockMarketLevel;
import com.spark.bitrade.vo.StoSubInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员CNYT市场等级mapper
 * @author Zhang Yanjun
 * @time 2018.12.03 20:01
 */
@Mapper
public interface LockMarketLevelMapper {
    //根据会员id查询
    LockMarketLevel findByMemberId(@Param("memberId") Long memberId, @Param("symbol") String symbol);

    //查询该会员的直接部门信息
    List<StoSubInfoVo> findSubInfoByMemberId(@Param("memberId") Long memberId, @Param("defaultLevel")String defaultLevel, @Param("symbol") String symbol);

}
