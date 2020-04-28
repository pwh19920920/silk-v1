package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.dao.LockBttcRestitutionIncomePlanDao;
import com.spark.bitrade.entity.LockBttcRestitutionIncomePlan;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.mapper.dao.LockBttcRestitutionIncomePlanMapper;
import com.spark.bitrade.service.LockBttcRestitutionIncomePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.annotations.Cacheable;

import java.util.Date;
import java.util.List;

@Service
public class LockBttcRestitutionIncomePlanServiceImpl extends ServiceImpl<LockBttcRestitutionIncomePlanMapper, LockBttcRestitutionIncomePlan> implements LockBttcRestitutionIncomePlanService {

    @Autowired
    private LockBttcRestitutionIncomePlanMapper lockBttcRestitutionIncomePlanMapper;

    @Autowired
    private LockBttcRestitutionIncomePlanDao lockBttcRestitutionIncomePlanDao;

    @Override
    @ReadDataSource
    public PageInfo<LockBttcRestitutionIncomePlan> findById(Long lockDetailId, Integer pageNo, Integer pageSize) {
        Page<LockBttcRestitutionIncomePlan> page = PageHelper.startPage(pageNo, pageSize);
        lockBttcRestitutionIncomePlanMapper.findById(lockDetailId);
        return page.toPageInfo();
    }

    @ReadDataSource
    @Override
    public List<LockBttcRestitutionIncomePlan> getCanRestitutionList(LockBackStatus status, int lockNum, Date rewardTime) {
        return lockBttcRestitutionIncomePlanMapper.findCanRestitutionList(status, lockNum, rewardTime);
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, LockBackStatus oldStatus,LockBackStatus newStatus) {
        return lockBttcRestitutionIncomePlanDao.updateStatus(id, oldStatus, newStatus) > 0 ? true : false;
    }

    @ReadDataSource
    @Override
    public LockBttcRestitutionIncomePlan findLastPlanByMemberIdAndDetailId(Long memberId,Long detailId) {
        return lockBttcRestitutionIncomePlanDao.findLastPlanByMemberIdAndDetailId(memberId, detailId);
    }

    @ReadDataSource
    @Override
    public  List<Long> findIeoUnlockDetailToday(){
        return lockBttcRestitutionIncomePlanDao.findIeoUnlockDetailToday();
    }

    @org.springframework.cache.annotation.Cacheable(cacheNames = "lockBttcRestitutionIncomePlan", key = "'entity:lockBttcRestitutionIncomePlan:findInnerList'")
    public  String findInnerList(){
        return lockBttcRestitutionIncomePlanDao.findInnerList();
    }

}
