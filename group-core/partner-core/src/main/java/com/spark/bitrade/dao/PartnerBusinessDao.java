package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.PartnerBusiness;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
  * 合伙人业务明细dao
  * @author tansitao
  * @time 2018/5/28 15:32 
  */
public interface PartnerBusinessDao extends BaseDao<PartnerBusiness> {


    @Query(value="select * from partner_business p where p.area_id = :areaId ORDER BY p.statistical_cycle DESC limit 1",nativeQuery = true)
    PartnerBusiness findPartnerBusinessByAreaId(@Param("areaId")String areaId);
}
