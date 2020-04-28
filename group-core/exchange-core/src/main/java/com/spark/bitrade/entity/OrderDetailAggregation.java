package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.OrderTypeEnum;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "order_detail_aggregation")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailAggregation implements Serializable{

    /**
     * 订单编号
     */
    private String orderId ;

    /**
     * 订单类型
     * otc 或者 exchange  order
     */
    private OrderTypeEnum type ;

    /**
     * 用户名
     */
    private String username ;

    /**
     * 会员真实姓名
     */
    private String realName ;

    /**
     * 会员ID
     */
    private Long memberId ;

    /**
     * 此聚合信息生成时间
     */
    private long time ;

    /**
     * 手续费
     */
    private double fee ;

    private double feeDiscount; //add by yangch 时间： 2018.06.01 原因：交易手续费优惠数量

    /**
     * 数量
     */
    private double amount ;

    /**
     * 币种单位
     */
    private String unit ;

    /**
     * exchange订单专有属性
     */
    private ExchangeOrderDirection direction;

    /**
     * 交易对象id
     * otc订单专有属性
     */
    private Long customerId;

    /**
     * 交易对象用户名
     * otc订单专有属性
     */
    private String customerName;

    private String customerRealName;

    private String refOrderId;  //add by yangch 时间： 2018.06.09 原因：关联交易的订单号

}
