package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.vo.CoinTokenVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.05 10:08
 */
@Mapper
public interface CoinTokenMapper {


    List<CoinTokenVo> queryCoinTokenList(Map<String,String> params);


    int updateById(CoinTokenVo coinTokenVo);

    int insertNew(CoinTokenVo coinTokenVo);

    int deleteById(@Param("id") Long id);

}
