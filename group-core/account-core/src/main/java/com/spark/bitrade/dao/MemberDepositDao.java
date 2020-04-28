package com.spark.bitrade.dao;

import com.spark.bitrade.entity.MemberDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.math.BigDecimal;

public interface MemberDepositDao extends JpaRepository<MemberDeposit,Long>,QueryDslPredicateExecutor<MemberDeposit>{
    MemberDeposit findByAddressAndTxidAndUnitAndAmount(String address, String txid, String unit, BigDecimal amount);
}
