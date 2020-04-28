package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockIeoRestitutionIncomePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface LockIeoRestitutionIncomePlanDao extends BaseDao<LockIeoRestitutionIncomePlan> {

    @Query(value = "SELECT * FROM lock_ieo_restitution_income_plan WHERE status = :status and reward_time <= :rewardTime limit :lockNum", nativeQuery = true)
    List<LockIeoRestitutionIncomePlan> getCanRestitutionList(@Param("status") Integer status, @Param("lockNum") int lockNum, @Param("rewardTime") Date rewardTime);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = " UPDATE LockIeoRestitutionIncomePlan a SET a.status = :newStatus where a.id = :id and a.status = :oldStatus")
    int updateStatus(@Param("id") Long id, @Param("oldStatus") LockBackStatus oldStatus, @Param("newStatus") LockBackStatus newStatus);

    @Query(value = "SELECT * FROM lock_ieo_restitution_income_plan WHERE status = 0 and member_id = :memberId limit :num", nativeQuery = true)
    List<LockIeoRestitutionIncomePlan> getLockIeoRestitutionIncomePlanList(@Param("memberId") Long memberId, @Param("num") Integer num);

    @Query(value = "SELECT sum(restitution_amount) from lock_ieo_restitution_income_plan WHERE status = 0 and member_id = :memberId",nativeQuery = true)
    BigDecimal getRestitutionIncomePlanAmount(@Param("memberId") Long memberId);
}
