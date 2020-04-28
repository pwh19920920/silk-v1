package com.spark.bitrade.dao;

import com.spark.bitrade.entity.PreCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author Zhang Jinwei
 * @date 2018年03月26日
 */
public interface PreCoinDao extends JpaRepository<PreCoin,Long>,JpaSpecificationExecutor<PreCoin> {

}
