package com.spark.bitrade.dao;

import com.spark.bitrade.entity.ExchangeMemberDiscountRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/***
 * 
 * @author yangch
 * @time 2018.08.28 10:32
 */
public interface ExchangeMemberDiscountRuleDao
        extends JpaRepository<ExchangeMemberDiscountRule, Long> {

    //获取所有优惠的会员ID
    @Query("select a.memberId from ExchangeMemberDiscountRule a where a.enable=1 group by a.memberId")
    List<Long> findMemberIds();

    @Query("select a from ExchangeMemberDiscountRule a where a.enable=1 ")
    List<ExchangeMemberDiscountRule> findAll();

    @Query("select a from ExchangeMemberDiscountRule a where a.memberId=:memberId and a.enable=1 ")
    List<ExchangeMemberDiscountRule> findAllByMemberId(@Param("memberId") long memberId);
}
