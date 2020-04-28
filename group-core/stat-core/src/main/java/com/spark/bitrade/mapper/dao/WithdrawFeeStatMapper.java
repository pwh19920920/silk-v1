package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.WithdrawFeeStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2018.12.17 15:36
 */
@Mapper
public interface WithdrawFeeStatMapper {

    List<WithdrawFeeStat> findAllBy(@Param("unit") String unit,@Param("startTime") String startTime,@Param("endTime") String endTime);
}
