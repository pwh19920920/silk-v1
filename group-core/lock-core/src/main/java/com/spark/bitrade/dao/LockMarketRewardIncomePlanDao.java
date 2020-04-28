package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockMarketRewardIncomePlan;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Zhang Yanjun
 * @time 2018.12.03 20:22
 */
public interface LockMarketRewardIncomePlanDao extends BaseDao<LockMarketRewardIncomePlan> {

    /**
     * 更新状态
     * @param id
     */
    @Modifying
    @Query("update LockMarketRewardIncomePlan a set a.status=:newStatus where a.id=:id and a.status=:oldStatus")
    int updateStatus(@Param("id") long id, @Param("newStatus") LockBackStatus newStatus, @Param("oldStatus") LockBackStatus oldStatus);
}
