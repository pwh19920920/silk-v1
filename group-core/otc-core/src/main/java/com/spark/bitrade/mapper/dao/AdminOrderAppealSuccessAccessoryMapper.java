package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.AdminOrderAppealSuccessAccessory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2018.11.01 19:40
 */
@Mapper
public interface AdminOrderAppealSuccessAccessoryMapper extends BaseMapper<AdminOrderAppealSuccessAccessory>{

    List<AdminOrderAppealSuccessAccessory> findByAppealId(@Param("appealId") Long appealId);
}
