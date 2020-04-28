package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.AdminDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author lingxing
 * @time 2018.07.30 15:46
 */
@Mapper
public interface AdminMapper {

    AdminDto findById(@Param("adminId") long adminId);
}
