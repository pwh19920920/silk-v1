package com.spark.bitrade.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.16 10:56  
 */
@Data
@Builder
public class LockUttDto {

    /**
     * 第一天释放比例
     */
    private BigDecimal firstRate;

    /**
     * 第31-131天释放比例
     */
    private BigDecimal avgRate;
    /**
     * 总帐户id
     */
    private Long totalMemberId;
    /**
     * 锁仓币种人民币价格
     */
    private BigDecimal coinCnyPrice;
    /**
     * 锁仓币种USDT价格
     */
    private BigDecimal coinUSDTPrice;
    /**
     * USDT的人民币价格
     */
    private BigDecimal usdtPrice;

}
