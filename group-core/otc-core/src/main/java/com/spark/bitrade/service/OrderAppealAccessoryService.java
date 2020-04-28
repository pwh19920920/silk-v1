package com.spark.bitrade.service;

import com.spark.bitrade.dao.OrderAppealAccessoryDao;
import com.spark.bitrade.entity.OrderAppealAccessory;
import com.spark.bitrade.service.Base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
  * 订单申诉servicr
  * @author tansitao
  * @time 2018/8/28 9:27 
  */
@Service
@Slf4j
public class OrderAppealAccessoryService extends BaseService<OrderAppealAccessory> {
    @Autowired
    private OrderAppealAccessoryDao orderAppealAccessoryDao;

    public OrderAppealAccessory save(OrderAppealAccessory orderAppealAccessory){
        return orderAppealAccessoryDao.saveAndFlush(orderAppealAccessory);
    }

    public OrderAppealAccessory findOne(long id){
        return orderAppealAccessoryDao.findById(id);
    }

    public List<OrderAppealAccessory> findByOtcApiAppealId(Long otcApiAppealId){
        return orderAppealAccessoryDao.findByOtcApiAppealId(otcApiAppealId);
    }
}
