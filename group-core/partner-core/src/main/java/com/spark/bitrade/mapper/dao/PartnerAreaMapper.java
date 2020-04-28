package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.vo.PartnerAreaVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author fumy
 * @time 2018.08.24 15:44
 */
@Mapper
public interface PartnerAreaMapper {

    List<PartnerAreaVo> getPartnerList(@Param("account") String account, @Param("areaId") String areaId);
}
