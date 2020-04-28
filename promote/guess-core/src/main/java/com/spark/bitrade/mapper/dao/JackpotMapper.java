package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.Jackpot;
import org.apache.ibatis.annotations.Param;

public interface JackpotMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Jackpot record);

    int insertSelective(Jackpot record);

    Jackpot selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Jackpot record);

    int updateByPrimaryKey(Jackpot record);

    /**
     * 通过期数id查询奖池信息
     * @author tansitao
     * @time 2018/9/14 10:58 
     */
    Jackpot selectByPeriodId(@Param("periodId") long periodId);
}