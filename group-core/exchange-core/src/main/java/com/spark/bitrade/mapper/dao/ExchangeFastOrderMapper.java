package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.constant.ExchangeOrderStatus;
import com.spark.bitrade.entity.ExchangeFastOrderDO;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExchangeFastOrderMapper extends SuperMapper<ExchangeFastOrderDO> {

    /**
     * 修改闪兑订单接收方的状态
     *
     * @param orderId       订单ID
     * @param oldStatus     修改前的状态
     * @param newStatus     修改后的状态
     * @param completedTime 完成时间
     * @return
     */
    int updataReceiverStatus(@Param("orderId") Long orderId,
                             @Param("oldStatus") ExchangeOrderStatus oldStatus,
                             @Param("newStatus") ExchangeOrderStatus newStatus,
                             @Param("completedTime") Long completedTime);
}
