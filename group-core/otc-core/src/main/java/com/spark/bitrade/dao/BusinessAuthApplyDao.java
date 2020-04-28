package com.spark.bitrade.dao;

import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BusinessAuthApply;
import com.spark.bitrade.entity.Member;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/7
 */
public interface BusinessAuthApplyDao extends BaseDao<BusinessAuthApply> {

    List<BusinessAuthApply> findByMemberOrderByIdDesc(Member member);

    List<BusinessAuthApply> findByMemberAndCertifiedBusinessStatusOrderByIdDesc(Member member, CertifiedBusinessStatus certifiedBusinessStatus);

    long countAllByCertifiedBusinessStatus(CertifiedBusinessStatus status);

    @Query(value = "SELECT * FROM business_auth_apply  WHERE member_id =:memberId ORDER BY create_time DESC LIMIT 1",nativeQuery = true)
    BusinessAuthApply findOneByMemberIdDesc(@Param("memberId")long memberId);

}
