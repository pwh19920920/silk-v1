package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 *
 * @author shenzucai
 * @time 2019.05.09 19:03
 */
@Mapper
public interface CoinMapper extends SuperMapper<Coin> {
    Coin findByUnit(@Param("unit") String unit);
}
