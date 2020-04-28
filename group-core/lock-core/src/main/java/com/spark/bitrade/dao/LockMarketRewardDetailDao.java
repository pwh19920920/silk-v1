package com.spark.bitrade.dao;

import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockMarketRewardDetail;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Zhang Yanjun
 * @time 2018.12.03 19:53
 */
public interface LockMarketRewardDetailDao extends BaseDao<LockMarketRewardDetail> {
    /**
     * 更新记录状态
     * @param id
     */
    @Modifying
    @Query("update LockMarketRewardDetail a set a.recordStatus=:newStatus where a.id=:id and a.recordStatus=:oldStatus")
    int updateRecordStatus(@Param("id") long id, @Param("newStatus") ProcessStatus newStatus, @Param("oldStatus") ProcessStatus oldStatus);

    /**
     * 更新业绩状态
     * @param id
     */
    @Modifying
    @Query("update LockMarketRewardDetail a set a.perUpdateStatus=:newStatus where a.id=:id and a.perUpdateStatus=:oldStatus")
    int updatePerUpdateStatus(@Param("id") long id, @Param("newStatus") ProcessStatus newStatus, @Param("oldStatus") ProcessStatus oldStatus);

    /**
     * 更新等级状态
     * @param id
     */
    @Modifying
    @Query("update LockMarketRewardDetail a set a.levUpdateStatus=:newStatus where a.id=:id and a.levUpdateStatus=:oldStatus")
    int updateLevUpdateStatus(@Param("id") long id, @Param("newStatus") ProcessStatus newStatus, @Param("oldStatus") ProcessStatus oldStatus);
}
