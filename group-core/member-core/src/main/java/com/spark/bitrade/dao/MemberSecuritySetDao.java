package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.MemberSecuritySet;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户安全认证dao
 * @author tansitao
 * @time 2018/7/5 9:24 
 */
public interface MemberSecuritySetDao extends BaseDao<MemberSecuritySet> {
    public MemberSecuritySet findById(long id);

    public MemberSecuritySet findByMemberId(long id);

    @Modifying
    @Query(value = "update member_security_set m set is_open_google_login = 0 where m.member_id = :id",nativeQuery = true)
    int updateSecurityStatus1(@Param("id") Long id);

    @Modifying
    @Query(value = "update member_security_set m set is_open_google_up_coin = 0 where m.member_id = :id",nativeQuery = true)
    int updateSecurityStatus2(@Param("id") Long id);

    @Modifying
    @Query(value = "update member_security_set m set is_open_phone_login = 0 where m.member_id = :id",nativeQuery = true)
    int updateSecurityStatus3(@Param("id") Long id);

    @Modifying
    @Query(value = "update member_security_set m set is_open_phone_up_coin = 0 where m.member_id = :id",nativeQuery = true)
    int updateSecurityStatus4(@Param("id") Long id);

}
