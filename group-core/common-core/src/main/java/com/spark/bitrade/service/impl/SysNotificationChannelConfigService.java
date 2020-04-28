package com.spark.bitrade.service.impl;

//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.entity.SysNotificationChannelConfig;
import com.spark.bitrade.mapper.dao.SysNotificationChannelConfigMapper;
import com.spark.bitrade.service.ISysNotificationChannelConfigService;
import com.spark.bitrade.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
  * 解锁锁仓活动服务实现类
  * @author tansitao
  * @time 2018/7/19 11:16 
  */
@Service
@Slf4j
public class SysNotificationChannelConfigService extends ServiceImpl<SysNotificationChannelConfigMapper, SysNotificationChannelConfig> implements ISysNotificationChannelConfigService {

    @Autowired
    private SysNotificationChannelConfigMapper mapper;

    @Override
    public List<SysNotificationChannelConfig> findByType(int type) {
        return  mapper.findByType(type);
    }
}
