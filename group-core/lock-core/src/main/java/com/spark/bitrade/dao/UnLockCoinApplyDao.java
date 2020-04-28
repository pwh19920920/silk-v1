package com.spark.bitrade.dao;

import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.UnlockCoinApply;

import java.util.List;

/**
 * @author jiangtao
 * @date 2018/5/17
 */
public interface UnLockCoinApplyDao extends BaseDao<UnlockCoinApply> {

    List<UnlockCoinApply> findByMemberAndStatusOrderByIdDesc(Member member, CertifiedBusinessStatus status);

    List<UnlockCoinApply> findByMemberOrderByIdDesc(Member member);

    long countAllByStatus(CertifiedBusinessStatus status);
}
