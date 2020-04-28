package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.entity.LockBttcRestitutionIncomePlan;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Date;


public interface LockBttcRestitutionIncomePlanMapper extends SuperMapper<LockBttcRestitutionIncomePlan> {

    List<LockBttcRestitutionIncomePlan> findById(@Param("lockDetailId")Long lockDetailId);


    /**
     * 查找可以返还的记录
     * @param status
     * @param lockNum
     * @param rewardTime
     * @return
     */
    List<LockBttcRestitutionIncomePlan> findCanRestitutionList(@Param("status")LockBackStatus status, @Param("lockNum")int lockNum, @Param("rewardTime")Date rewardTime);

    /**
     * 更新返还状态
     * @param id
     * @param beforeStatus
     * @param newStatus
     * @return
     */
    int updateStatus(@Param("id")Long id, @Param("beforeStatus")LockBackStatus beforeStatus, @Param("newStatus")LockBackStatus newStatus);
}
