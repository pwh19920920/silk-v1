package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.UnlockCoinTask;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/***
 * 锁仓活动解锁任务
 * @author yangch
 * @time 2018.08.02 11:58
 */
//@Repository
public interface UnlockCoinTaskDao extends BaseDao<UnlockCoinTask> {
    //查询最近的一条记录
    UnlockCoinTask findFirstByRefActivitieIdOrderByIdDesc(Long refActivitieId);

    /**
     *  修改解锁任务状态
     * @author tansitao
     * @time 2018/8/7 15:12 
     */
    @Modifying
    @Query("update UnlockCoinTask a set a.status=:nowStatus where a.id=:id and a.status=:oldStatus")
    int updateUnlockCoinTaskStatus(@Param("id") long id, @Param("nowStatus") ProcessStatus nowStatus, @Param("oldStatus") ProcessStatus oldStatus);
}
