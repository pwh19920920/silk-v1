package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.AlarmMonitorDao;
import com.spark.bitrade.entity.AlarmMonitor;
import com.spark.bitrade.mapper.dao.AlarmMonitorMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 告警监控service
 * @author Zhang Yanjun
 * @time 2018.09.27 14:13
 */
@Service
public class AlarmMonitorService extends BaseService{

    @Autowired
    AlarmMonitorDao alarmMonitorDao;
    @Autowired
    AlarmMonitorMapper alarmMonitorMapper;

    public AlarmMonitor save(AlarmMonitor alarmMonitor){
        return alarmMonitorDao.save(alarmMonitor);
    }

    //分页
    public PageInfo<AlarmMonitor> findAllBy(Long memberId,Long alarmType,int pageNo, int pageSize){
        Page<AlarmMonitor> page= PageHelper.startPage(pageNo, pageSize);
        this.alarmMonitorMapper.findAllBy(memberId,alarmType);
        return page.toPageInfo();
    }

    //根据id查找
    public AlarmMonitor findOneById(Long id){
        return alarmMonitorDao.findOne(id);
    }

    public int updateAlarMonitor(BooleanEnum status,Date nowDate,Long id){
        return alarmMonitorDao.updateAlarmMonitorStatus(status,nowDate,id);
    }

}
