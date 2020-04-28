package com.spark.bitrade.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.23 15:12  
 */
@Data
@Accessors(chain = true)
public class MemberOrderCount {

    /**
     * 广告id
     */
    private Long adverId;
    /**
     * 用户id 商家用户id
     */
    private Long memberId;
    /**
     * 正在进行中的订单数
     */
    private Long tradingCounts;
    /**
     * 48小时内的接单数
     */
    private Long count48;
    /**
     * 48小时内的接单金额
     */
    private BigDecimal money48;
    /**
     * 单价
     */
    private BigDecimal price;
    /**
     * 交易量
     */
    private int hasTrade;
    /**
     * 置顶值
     */
    private int sort;
}
