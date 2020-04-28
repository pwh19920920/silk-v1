package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.QuantifyLockReWard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.08.23 15:23
 */
@Mapper
public interface QuantifyLockReWardMapper {

    QuantifyLockReWard getByMemberId(@Param("memberId") Long memberId);

    List<QuantifyLockReWard> getByPage(@Param("memberId") Long memberId);

    Long insertVip(Map<String,Object> params);
}
