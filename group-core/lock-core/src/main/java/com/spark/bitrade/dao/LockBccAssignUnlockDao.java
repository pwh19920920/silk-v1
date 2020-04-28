package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockBccAssignRecord;
import com.spark.bitrade.entity.LockBccAssignUnlock;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LockBccAssignUnlockDao extends BaseDao<LockBccAssignUnlock> {

    @Query(value = "select * from lock_bcc_assign_unlock where member_id=:memberId and release_type=1", nativeQuery = true)
    List<LockBccAssignUnlock> findIeoLockBccAssignUnlock(@Param("memberId") Long memberId);

}
