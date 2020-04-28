package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.ThirdPlatformApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author fumy
 * @time 2018.10.23 15:41
 */
@Mapper
public interface PayApplyMapper {

    List<ThirdPlatformApply> getApplyList(@Param("busiAccount")String busiAccount,@Param("status")Integer status);
}
