package com.spark.bitrade.mapper.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Zhang Yanjun
 * @time 2018.09.10 18:56
 */
@Mapper
public interface ExchangeCoinMapper {
    List<Map<String,Object>> findTraderDiscount(@Param("symbol") String symbol, @Param("coinSymbol") String coinSymbol, @Param("baseSymbol") String baseSymbol);
}
