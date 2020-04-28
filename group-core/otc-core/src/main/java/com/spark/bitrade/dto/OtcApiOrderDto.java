package com.spark.bitrade.dto;

import com.spark.bitrade.constant.OrderStatus;
import lombok.Data;

import java.util.Date;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.30 09:40  
 */
@Data
public class OtcApiOrderDto {

    private Long id;

    private Long refOtcOrderId;

    private String orderSn;

    private OrderStatus status;

    private Date payTime;

    private Long memberId;

    private Long tradeId;

    private Long organizationId;
}
