package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.HotTransferRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 热钱包转入冷钱包mapper
 * @author Zhang Yanjun
 * @time 2018.09.04 16:38
 */
@Mapper
public interface HotTransferRecordMapper {

    List<HotTransferRecord> findAllBy(@Param("adminName") String adminName,@Param("coldAddress") String coldAddress,@Param("unit") String unit);
}
