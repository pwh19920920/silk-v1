package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.TechRechargeRecordDto;
import com.spark.bitrade.entity.TechRechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lingxing
 * @time 2018.07.17 18:35
 */
@Mapper
public interface TechRechargeRecordMapper {
    List<TechRechargeRecord>findByTechRechargeRecord(@Param("techRechargeRecord")TechRechargeRecordDto techRechargeRecordDto);
}
