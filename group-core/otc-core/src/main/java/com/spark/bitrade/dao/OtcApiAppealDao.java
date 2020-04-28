package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.OtcApiAppeal;

import java.util.List;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.30 14:24  
 */
public interface OtcApiAppealDao extends BaseDao<OtcApiAppeal> {
    /**
     * 查询otcApiAppeal
     * @param orderId
     * @return
     */
    List<OtcApiAppeal> findByOtcApiOrderIdOrderByCreateTimeDesc(Long orderId);

}
