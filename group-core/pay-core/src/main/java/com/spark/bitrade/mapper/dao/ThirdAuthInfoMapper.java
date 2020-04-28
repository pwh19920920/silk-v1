package com.spark.bitrade.mapper.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.19 16:17
 */
@Mapper
public interface ThirdAuthInfoMapper {

    @Select("select count(id) from thrid_auth_info where member_id=#{memberId} and symbol=#{symbol} and status=0")
    int getAuthByMerberIdAndSymbol(@Param("memberId") Long memberId,@Param("symbol") String symbol);


    @Select("select id,symbol from thrid_auth_info where member_id=#{memberId}")
    List<Map<String,String>> getAuthCoin(@Param("memberId") Long memberId);
}
