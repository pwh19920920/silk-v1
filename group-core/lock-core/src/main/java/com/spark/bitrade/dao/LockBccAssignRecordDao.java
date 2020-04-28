package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockBccAssignRecord;
import com.spark.bitrade.entity.LockIeoRestitutionIncomePlan;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface LockBccAssignRecordDao extends BaseDao<LockBccAssignRecord> {

    @Query(value = "select * from lock_bcc_assign_record where member_id=:memberId and status = 1 ", nativeQuery = true)
    LockBccAssignRecord findByMemberId(@Param("memberId") Long memberId);

    @Query(value = "select SUM(lock_portion) from lock_bcc_assign_record where superior_id=:superiorId", nativeQuery = true)
    Integer findSubLockPortion(@Param("superiorId") Long superiorId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update lock_bcc_assign_record set release_portion = release_portion - :releaseCommissionPortion," +
            "release_amount = release_amount- :releaseCommissionAmount where id=:id", nativeQuery = true)
    Integer updateByMemberId(@Param("id") Long id,
                             @Param("releaseCommissionPortion") Integer releaseCommissionPortion,
                             @Param("releaseCommissionAmount") BigDecimal releaseCommissionAmount);
    @Modifying(clearAutomatically = true)
    @Query(value = "update lock_bcc_assign_record set reward_portion = reward_portion - IF(:clearPortion <= release_portion,:clearPortion,release_portion)," +
            "release_amount = release_amount- IF(:clearPortion <= release_portion,:clearPortion,release_portion) * :perAmount," +
            "reward_amount = reward_amount - IF(:clearPortion <= release_portion,:clearPortion,release_portion) * :perAmount," +
            "release_portion = release_portion - IF(:clearPortion <= release_portion,:clearPortion,release_portion) where member_id=:memberId",nativeQuery = true)
    Integer mounthClearUpdateByMemberId(@Param("memberId") Long memberId,
                             @Param("clearPortion") Integer clearPortion,
                             @Param("perAmount") BigDecimal perAmount);
}
