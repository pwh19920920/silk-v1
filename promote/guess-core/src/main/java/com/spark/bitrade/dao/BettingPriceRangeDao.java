package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BettingPriceRange;

import java.util.List;

/**
 * @author fumy
 * @time 2018.09.13 16:23
 */
public interface BettingPriceRangeDao  extends BaseDao<BettingPriceRange>{
    List<BettingPriceRange> findAllByPeriodId(long periodId);
}
