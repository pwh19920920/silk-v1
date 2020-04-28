package com.spark.bitrade.service;

import com.spark.bitrade.dao.AdminOrderAppealSuccessAccessoryDao;
import com.spark.bitrade.entity.AdminOrderAppealSuccessAccessory;
import com.spark.bitrade.mapper.dao.AdminOrderAppealSuccessAccessoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 场外订单申诉处理service
 * @author Zhang Yanjun
 * @time 2018.10.31 10:32
 */
@Service
public class AdminOrderAppealSuccessAccessoryService{

    @Autowired
    private AdminOrderAppealSuccessAccessoryDao adminOrderAppealSuccessAccessoryDao;
    @Autowired
    private AdminOrderAppealSuccessAccessoryMapper adminOrderAppealSuccessAccessoryMapper;

    public AdminOrderAppealSuccessAccessory save(AdminOrderAppealSuccessAccessory adminOrderAppealSuccessAccessory){
        return adminOrderAppealSuccessAccessoryDao.save(adminOrderAppealSuccessAccessory);
    }

    public List<AdminOrderAppealSuccessAccessory> findByAppealId(Long appealId){
//        return adminOrderAppealSuccessAccessoryDao.findByAppealId(appealId);
        return adminOrderAppealSuccessAccessoryMapper.findByAppealId(appealId);
    }


}
