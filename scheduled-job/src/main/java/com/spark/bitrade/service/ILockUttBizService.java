package com.spark.bitrade.service;

import com.spark.bitrade.constant.TransactionType;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.15 17:17  
 */
public interface ILockUttBizService {


    void lockUtt(String batchNum);

    void releaseUtt(TransactionType type);
}
