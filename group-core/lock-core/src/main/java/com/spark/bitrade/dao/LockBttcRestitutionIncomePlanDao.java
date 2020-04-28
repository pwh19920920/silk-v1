package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockBttcRestitutionIncomePlan;
import com.spark.bitrade.entity.LockCoinDetail;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LockBttcRestitutionIncomePlanDao extends BaseDao<LockBttcRestitutionIncomePlan> {

    @Modifying
    @Query(value = "UPDATE LockBttcRestitutionIncomePlan SET status = ?3 where id = ?1 and status = ?2")
    int updateStatus(Long id, LockBackStatus oldStatus, LockBackStatus newStatus);

    @Query(value = "SELECT  * FROM lock_bttc_restitution_income_plan   where member_id = :memberId and period = :detailId and status = 0 order by id  DESC LIMIT 1  ", nativeQuery = true)
    LockBttcRestitutionIncomePlan findLastPlanByMemberIdAndDetailId( @Param("memberId")Long memberId,@Param("detailId")Long detailId);

    @Query(value = "SELECT id  FROM  lock_coin_detail   where  type = 7 and DATE_FORMAT(plan_unlock_time,'%Y-%m-%d') = DATE_FORMAT(now(),'%Y-%m-%d') and coin_unit = 'BTTC' and status = 0    ", nativeQuery = true)
    List<Long> findIeoUnlockDetailToday();

    @Query(value = "SELECT members from lock_bttc_ieo_particulars where id=1   ", nativeQuery = true)
    String findInnerList();
}
