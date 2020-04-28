package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.PartnerBusinessMonth;

/**
  * 合伙人业务月统计dao
  * @author tansitao
  * @time 2018/5/28 15:32 
  */
public interface PartnerBusinessMonthDao extends BaseDao<PartnerBusinessMonth> {


    PartnerBusinessMonth findPartnerBusinessMonthByAreaIdAndStatisticalCycle(String areaId,String collectTime);
}
