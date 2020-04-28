package com.spark.bitrade.dao;

import com.spark.bitrade.entity.OrderDetailAggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderDetailAggregationRepository extends MongoRepository<OrderDetailAggregation,String>{

    //List<OrderDetailAggregation> findAll(Specification<OrderDetailAggregation> specification);

    //Page<OrderDetailAggregation> findAll(Specification<OrderDetailAggregation> specification, Pageable pageable);

    List<OrderDetailAggregation> findAllByTimeGreaterThanEqualAndTimeLessThanAndUnit(long var1,long var2,String var3);

    //add by yangch 时间： 2018.06.09 原因：删除指定订单ID和关联订单的记录
    int deleteOrderDetailAggregationByOrderIdAndRefOrderId(String orderId, String refOrderId);

}

