package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.dao.LockBccAssignUnlockDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.LockBccAssignUnlockMapper;
import com.spark.bitrade.mapper.dao.LockBttcRestitutionIncomePlanMapper;
import com.spark.bitrade.service.LockBccAssignRecordService;
import com.spark.bitrade.service.LockBccAssignUnlockService;
import com.spark.bitrade.service.LockBttcRestitutionIncomePlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LockBccAssignUnlockServiceImpl extends ServiceImpl<LockBccAssignUnlockMapper, LockBccAssignUnlock> implements LockBccAssignUnlockService {

    @Override
    public PageInfo<LockBccAssignUnlock> findLockBccAssignUnlocksForPage(Long memberId, Integer pageNo, Integer pageSize) {
        Page<LockBccAssignUnlock> page = PageHelper.startPage(pageNo, pageSize);
        //PageHelper会自动拦截到下面这查询sql
        this.baseMapper.findLockBccAssignUnlocksByMemberId(memberId);

        return page.toPageInfo();
    }
}
