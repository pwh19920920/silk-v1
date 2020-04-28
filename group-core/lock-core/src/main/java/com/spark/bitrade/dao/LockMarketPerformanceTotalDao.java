package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockMarketPerformanceTotal;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/**
 * @author Zhang Yanjun
 * @time 2018.12.03 19:53
 */
public interface LockMarketPerformanceTotalDao extends BaseDao<LockMarketPerformanceTotal> {

    /**
      * 更新业绩
      * @author tansitao
      * @time 2018/12/5 18:12 
      */
    @Modifying
    @Query("update LockMarketPerformanceTotal set subDepartmentAmountTotal = subDepartmentAmountTotal + :performanceTurnover where memberId = :memberId")
    int updataPerformance(@Param("performanceTurnover")BigDecimal performanceTurnover, @Param("memberId")Long memberId);
}
