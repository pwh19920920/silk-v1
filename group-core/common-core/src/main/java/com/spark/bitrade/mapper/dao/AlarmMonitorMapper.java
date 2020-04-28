package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.AlarmMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 告警监控mapper
 * @author Zhang Yanjun
 * @time 2018.09.27 16:35
 */
@Mapper
public interface AlarmMonitorMapper {

    //按条件查询所有
    List<AlarmMonitor> findAllBy(@Param("memberId") Long memberId,@Param("alarmType") Long alarmType);
}
