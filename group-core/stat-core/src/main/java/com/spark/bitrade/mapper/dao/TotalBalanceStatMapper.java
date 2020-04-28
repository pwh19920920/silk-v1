package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.TotalBalanceStat;
import com.spark.bitrade.vo.TotalBalanceVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author fumy
 * @time 2018.09.27 19:30
 */
@Mapper
public interface TotalBalanceStatMapper {

    List<TotalBalanceStat> getDayOfTotal(@Param("unit") String unit);

    TotalBalanceVo getWalletBalance(@Param("unit") String unit, @Param("opDate") String opDate);

}
