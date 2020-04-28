package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.LockHqbInRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LockHqbInRecordMapper extends BaseMapper<LockHqbInRecord> {


    // @Select("SELECT * FROM lock_hqb_in_record WHERE (status = 0 AND apply_time < #{applyTime}) ORDER BY apply_time DESC limit ${beg}, ${size}")
    List<LockHqbInRecord> selectAsPage(@Param("applyTime") long applyTime, @Param("appId") Long appId, @Param("coinSymbol") String coinSymbol, @Param("beg") int beg, @Param("size") int size);
}
