package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.MemberPaymentAccount;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
  * 用户支付账户dao
  * @author tansitao
  * @time 2018/8/16 10:10 
  */
public interface MemberPaymentAccountDao extends BaseDao<MemberPaymentAccount> {
    MemberPaymentAccount findById(long id);

    @Modifying
    @Query(value = "delete from  member_payment_account  where member_id=:memberId",nativeQuery = true)
    int deleteEpayByMemberId(@Param("memberId")Long memberId);
}
