package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.AsyncNotificationBusiness;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

/**
 * H5GameRecordMapper
 *
 * @author archx
 * @time 2019/4/25 11:44
 */
@Mapper
public interface AsyncNotificationBusinessMapper extends BaseMapper<AsyncNotificationBusiness> {
}
