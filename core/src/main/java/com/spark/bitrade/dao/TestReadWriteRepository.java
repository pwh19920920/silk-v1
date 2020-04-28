package com.spark.bitrade.dao;


import com.spark.bitrade.entity.TestReadWrite;
import org.springframework.data.jpa.repository.JpaRepository;

/***
 * jpa测试接口
  *
 * @author yangch
 * @time 2018.06.20 14:08
 */
public interface TestReadWriteRepository extends JpaRepository<TestReadWrite, String> {
}
