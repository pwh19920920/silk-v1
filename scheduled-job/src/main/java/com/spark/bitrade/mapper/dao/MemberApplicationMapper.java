package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.MemberApplication;
import com.spark.bitrade.entity.MemberApplicationForjob;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.07 09:57
 */
@Mapper
public interface MemberApplicationMapper extends BaseMapper<MemberApplicationForjob> {

    List<MemberApplicationForjob> getNoAuditList();

}
