package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.MemberApplication;
import com.spark.bitrade.entity.MemberApplicationForjob;

import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.07 10:20
 */
public interface IMemberApplicationService extends IService<MemberApplicationForjob> {

    List<MemberApplicationForjob> getNoAuditList();

}
