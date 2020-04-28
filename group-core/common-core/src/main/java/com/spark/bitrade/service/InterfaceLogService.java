package com.spark.bitrade.service;

import com.spark.bitrade.dao.InterfaceLogDao;
import com.spark.bitrade.entity.InterfaceLog;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 接口日志操作service
 * @author tansitao
 * @time 2018/5/2 16:58 
 *
 */
@Service
public class InterfaceLogService extends BaseService {

    @Autowired
    private InterfaceLogDao interfaceLogDao;

    public InterfaceLog saveLog(InterfaceLog interfaceLog) {
        return interfaceLogDao.save(interfaceLog);
    }

}
