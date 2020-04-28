package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.AlarmMonitor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * 告警监控dao
 * @author Zhang Yanjun
 * @time 2018.09.27 15:58
 */
public interface AlarmMonitorDao extends BaseDao<AlarmMonitor> {

    @Modifying
    @Query("update AlarmMonitor a set a.status=:status  ,a.maintenanceTime=:nowDate where a.id=:id")
    int updateAlarmMonitorStatus(@Param("status") BooleanEnum status,  @Param("nowDate") Date nowDate, @Param("id") Long id);

}
