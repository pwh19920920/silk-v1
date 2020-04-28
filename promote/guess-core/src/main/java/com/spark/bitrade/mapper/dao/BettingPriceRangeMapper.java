package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.BettingPriceRange;
import com.spark.bitrade.vo.PriceRangeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BettingPriceRangeMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BettingPriceRange record);

    int insertSelective(BettingPriceRange record);

    BettingPriceRange selectByPrimaryKey(@Param("id") Long id);

    int updateByPrimaryKeySelective(BettingPriceRange record);

    int updateByPrimaryKey(BettingPriceRange record);

    /**
     * 通过期数ID查询投票区域信息
     * @author tansitao
     * @time 2018/9/14 11:31 
     */
    List<BettingPriceRange> selectByPeriodId(@Param("periodId") long periodId);
}