package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.BusinessErrorMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 撮单
 * @author Zhang Yanjun
 * @time 2018.07.21 16:42
 */
@Mapper
public interface BusinessErrorMonitorMapper {

    //根据条件动态查询
    List<BusinessErrorMonitor> findBy(@Param("type") Integer type, @Param("maintenanceStatus")Integer maintenanceStatus, @Param("timeSort")int timeSort);

    //查询所有未处理的撮单个数
    int findUnMaintenanceStatus();
}
