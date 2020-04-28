package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.TraderWithdrawFeeStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2018.12.17 15:36
 */
@Mapper
public interface TraderWithdrawFeeStatMapper {

    List<TraderWithdrawFeeStat> findAllBy(@Param("unit") String unit, @Param("startTime") String startTime, @Param("endTime") String endTime);
}
