package com.spark.bitrade.dao;

import com.spark.bitrade.entity.ExchangeOrderDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExchangeOrderDetailRepository extends MongoRepository<ExchangeOrderDetail,String>{
    List<ExchangeOrderDetail> findAllByOrderId(String orderId);

    //add by yangch 时间： 2018.06.05 原因：根据订单号和关联订单查询记录数
    int countExchangeOrderDetailByOrderIdAndRefOrderId(String orderId, String refOrderId);

    //add by yangch 时间： 2018.06.29 原因：查询第一个满足条件的明细
    ExchangeOrderDetail findFirstByOrderIdAndRefOrderId(String orderId, String refOrderId);

    //add by yangch 时间： 2018.06.09 原因：删除指定订单ID和关联订单的记录
    int deleteExchangeOrderDetailByOrderIdAndRefOrderId(String orderId, String refOrderId);
}
