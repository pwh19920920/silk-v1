package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.DimArea;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
  * 定位信息查询dao
  * @author tansitao
  * @time 2018/5/12 11:27 
  */
public interface DimAreaDao extends BaseDao<DimArea> {

    DimArea findDimAreaByAreaId(String areaId);

    List<DimArea> findAllByFatherId(String fatherId);

    @Query(value="select * from dim_area where area_abbr_name LIKE :areaAbbrName and area_name like :areaName limit 1",nativeQuery = true)
    DimArea findOneByArea(@Param("areaAbbrName")String areaAbbrName, @Param("areaName")String areaName);

    /**
     * 根据区域等级查询相关区域
     * @author shenzucai
     * @time 2018.05.30 11:40
     * @param level
     * @return true
     */
    List<DimArea> findAllByLevel(String level);

}
