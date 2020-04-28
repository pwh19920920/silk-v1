package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.MemberApplicationForjob;
import com.spark.bitrade.mapper.dao.MemberApplicationMapper;
import com.spark.bitrade.service.IMemberApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author fumy
 * @time 2018.09.07 10:25
 */
@Service
@Slf4j
public class MemberApplicationServiceImpl extends ServiceImpl<MemberApplicationMapper,MemberApplicationForjob> implements IMemberApplicationService {

    @Autowired
    MemberApplicationMapper mapper;

    @Override
    public List<MemberApplicationForjob> getNoAuditList() {
        return mapper.getNoAuditList();
    }
}
