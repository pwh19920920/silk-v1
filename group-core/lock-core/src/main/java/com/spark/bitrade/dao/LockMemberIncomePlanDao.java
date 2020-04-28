package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockMemberIncomePlan;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

/**
 * @author Zhang Yanjun
 * @time 2018.12.03 19:53
 */
public interface LockMemberIncomePlanDao extends BaseDao<LockMemberIncomePlan> {

    /**
     * 更新状态
     * @param id
     */
    @Modifying
    @Query("update LockMemberIncomePlan a set a.status=:newStatus, a.updateTime=:updateTime where a.id=:id and a.status=:oldStatus ")
    int updateStatus(@Param("id") long id, @Param("newStatus") LockBackStatus newStatus, @Param("oldStatus") LockBackStatus oldStatus, @Param("updateTime") Date updateTime);
}
