package com.spark.bitrade.dao;


import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.MemberTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface MemberTransactionDao extends BaseDao<MemberTransaction> {

    @Query("select m from MemberTransaction as m where m.createTime between :startTime and  :endTime and m.type = :type")
    List<MemberTransaction> findAllDailyMatch(String startTime,String endTime,TransactionType type);

    @Modifying
    @Query(value = "update member_transaction set create_time = :date where id = :id", nativeQuery = true)
    int updateCreateTime(@Param("date") Date date, @Param("id") Long id);
}
