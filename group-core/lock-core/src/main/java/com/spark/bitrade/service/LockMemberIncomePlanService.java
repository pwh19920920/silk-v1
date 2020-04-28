package com.spark.bitrade.service;

import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.dao.LockMemberIncomePlanDao;
import com.spark.bitrade.entity.LockMemberIncomePlan;
import com.spark.bitrade.mapper.dao.LockMemberIncomePlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 收益返还计划service
 * @author Zhang Yanjun
 * @time 2018.12.03 20:07
 */
@Service
public class LockMemberIncomePlanService {

    @Autowired
    LockMemberIncomePlanDao lockMemberIncomePlanDao;
    @Autowired
    LockMemberIncomePlanMapper lockMemberIncomePlanMapper;

    /**
     * 新增
     * @author Zhang Yanjun
     * @time 2018.12.03 20:09
     * @param lockMemberIncomePlan
     */
    @CacheEvict(cacheNames = "lockMemberIncomePlan", key = "'entity:lockMemberIncomePlan:'+#lockMemberIncomePlan.getId()")
    public void save(LockMemberIncomePlan lockMemberIncomePlan){
        lockMemberIncomePlanDao.save(lockMemberIncomePlan);
    }

    /**
     * 更新状态
     * @author Zhang Yanjun
     * @time 2018.12.03 20:11
     * @param id
     * @param newStatus
     * @param oldStatus
     */
    @CacheEvict(cacheNames = "lockMemberIncomePlan", key = "'entity:lockMemberIncomePlan:'+#id")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, LockBackStatus newStatus, LockBackStatus oldStatus){
        return lockMemberIncomePlanDao.updateStatus(id, newStatus, oldStatus, new Date()) > 0 ? true : false;
    }

    /**
     * 待返还列表
     * @author Zhang Yanjun
     * @time 2018.12.03 20:17
     * @param
     */
    @Cacheable(cacheNames = "lockMemberIncomePlan", key = "'entity:lockMemberIncomePlan:list'")
    @ReadDataSource
    public List<LockMemberIncomePlan> findAllByBack(){
        return lockMemberIncomePlanMapper.findAllByBack();
    }

    /**
     * 根据id查询
     * @author Zhang Yanjun
     * @time 2018.12.03 20:19
     * @param id
     */
    @Cacheable(cacheNames = "lockMemberIncomePlan", key = "'entity:lockMemberIncomePlan:'+#id")
    @ReadDataSource
    public LockMemberIncomePlan findOneById(Long id){
        return lockMemberIncomePlanMapper.findOneById(id);
    }

    /**
     * 根据锁仓记录Id统计待返还的收益计划期数
     * @author fumy
     * @time 2018.12.06 15:33
     * @param lockCoinDetailId
     * @return true
     */
    @ReadDataSource
    public int countWaitBack(Long lockCoinDetailId){
        return lockMemberIncomePlanMapper.countWaitBackByLockDetailId(lockCoinDetailId);
    }
}
