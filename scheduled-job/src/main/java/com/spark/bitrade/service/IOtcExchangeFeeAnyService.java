package com.spark.bitrade.service;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.09 17:38  
 */
public interface IOtcExchangeFeeAnyService {
    /**
     * 手续费归集统计
     * @param yesterdayStart
     * @param yesterdayEnd
     * @param id
     */
    void run(String yesterdayStart, String yesterdayEnd, Long id);
}
