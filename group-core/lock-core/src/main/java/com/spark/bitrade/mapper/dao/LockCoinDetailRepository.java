package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockCoinDetail;
import org.springframework.data.jpa.repository.JpaRepository;

/***
 * jpa测试接口
  *
 * @author yangch
 * @time 2018.06.20 14:08
 */
public interface LockCoinDetailRepository extends JpaRepository<LockCoinDetail, String> {
}
