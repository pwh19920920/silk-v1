package com.spark.bitrade.dao;


import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.OrderAppealAccessory;

import java.util.List;


/**
 * 订单申诉dao
 * @author tansitao
 * @time 2018/8/28 9:26 
 */
public interface OrderAppealAccessoryDao extends BaseDao<OrderAppealAccessory> {
    public OrderAppealAccessory findById(long id);


    List<OrderAppealAccessory> findByOtcApiAppealId(Long appealId);
}
