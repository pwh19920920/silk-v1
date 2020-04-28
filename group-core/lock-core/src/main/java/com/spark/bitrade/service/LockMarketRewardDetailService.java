package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.dao.LockMarketRewardDetailDao;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.entity.LockMarketRewardDetail;
import com.spark.bitrade.entity.LockMarketRewardIncomePlan;
import com.spark.bitrade.mapper.dao.LockMarketRewardDetailMapper;
import com.spark.bitrade.vo.StoLockDepDetailVo;
import com.spark.bitrade.vo.StoLockDepVo;
import com.spark.bitrade.vo.StoLockIncomeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 市场奖励明细service
 * @author Zhang Yanjun
 * @time 2018.12.03 20:48
 */
@Service
public class LockMarketRewardDetailService {
    @Autowired
    LockMarketRewardDetailDao lockMarketRewardDetailDao;

    @Autowired
    LockMarketRewardDetailMapper lockMarketRewardDetailMapper;

    /**
     * 新增
     * @author Zhang Yanjun
     * @time 2018.12.03 20:24
     * @param lockMarketRewardDetail
     */
    @CacheEvict(cacheNames = "lockMarketRewardDetail", key = "'entity:lockMarketRewardDetail:'+#lockMarketRewardDetail.getId()")
    public LockMarketRewardDetail save (LockMarketRewardDetail lockMarketRewardDetail){
       return lockMarketRewardDetailDao.saveAndFlush(lockMarketRewardDetail);
    }

    /**
     * 更新记录状态
     * @author Zhang Yanjun
     * @time 2018.12.03 20:11
     * @param id
     * @param status
     */
    @CacheEvict(cacheNames = "lockMarketRewardDetail", key = "'entity:lockMarketRewardDetail:'+#id")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRecordStatus(Long id , ProcessStatus newStatus, ProcessStatus oldStatus){
        return lockMarketRewardDetailDao.updateRecordStatus(id, newStatus, oldStatus) > 0 ? true : false;
    }

    /**
     * 更新业绩状态
     * @author Zhang Yanjun
     * @time 2018.12.03 20:11
     * @param id
     * @param status
     */
    @CacheEvict(cacheNames = "lockMarketRewardDetail", key = "'entity:lockMarketRewardDetail:'+#id")
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePerUpdateStatus(Long id , ProcessStatus newStatus ,ProcessStatus oldStatus){
        return lockMarketRewardDetailDao.updatePerUpdateStatus(id,newStatus,oldStatus) > 0 ? true : false;
    }

    /**
     * 更新等级状态
     * @author Zhang Yanjun
     * @time 2018.12.03 20:11
     * @param id
     * @param status
     */
    @CacheEvict(cacheNames = "lockMarketRewardDetail", key = "'entity:lockMarketRewardDetail:'+#id")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLevUpdateStatus(Long id ,ProcessStatus newStatus ,ProcessStatus oldStatus){
        return lockMarketRewardDetailDao.updateLevUpdateStatus(id, newStatus, oldStatus) > 0 ? true : false;
    }

    /**
     * 根据id查询
     * @author Zhang Yanjun
     * @time 2018.12.03 20:19
     * @param id
     */
    @Cacheable(cacheNames = "lockMarketRewardDetail", key = "'entity:lockMarketRewardDetail:'+#id")
    @WriteDataSource
    public LockMarketRewardDetail findOneById(Long id){
        return lockMarketRewardDetailMapper.findOneById(id);
    }

    /**
      * 通过锁仓id和用户id查询奖励明细
      * @author tansitao
      * @time 2018/12/5 14:10 
      */
    @ReadDataSource
    public LockMarketRewardDetail findOneByLockDetailAndMember(Long lockDetailId, Long memberId){
        return lockMarketRewardDetailMapper.findOneByLockDetailAndMemberId(lockDetailId, memberId);
    }


    /**
     * 查询部门锁仓记录
     * @author Zhang Yanjun
     * @time 2018.12.04 15:32
     * @param memberId
     * @param startTime
     * @param endTime
     */
    @ReadDataSource
    public PageInfo<StoLockDepDetailVo> findDepByInviter(Long memberId,String symbol, String startTime, String endTime, int pageNo, int pageSize){
        Page<StoLockDepDetailVo> page = PageHelper.startPage(pageNo, pageSize);
        lockMarketRewardDetailMapper.findDepByInviter(memberId,symbol,startTime, endTime);
        return page.toPageInfo();
    }

    /**
     * 用户当前职务
     * @author Zhang Yanjun
     * @time 2018.12.04 17:06
     * @param memberId
     */
    @ReadDataSource
    public String findLevelByMemberId(Long memberId){
        return lockMarketRewardDetailMapper.findLevelByMemberId(memberId);
    }

    /**
     * 获取用户奖励收益记录
     * @author fumy
     * @time 2018.12.05 11:27
     * @param memberId
     * @return true
     */
    @ReadDataSource
    public PageInfo<StoLockIncomeVo> findMemberRewardIncome(Long memberId,String symbol, String startTime,String endTime, int pageNo, int pageSize){
        Page<StoLockIncomeVo> page = PageHelper.startPage(pageNo,pageSize);
        lockMarketRewardDetailMapper.queryMemberRewardIncome(memberId,symbol,startTime,endTime);
        return page.toPageInfo();
    }

    /**
     * 获取用户子部门锁仓汇总信息
     */
    public PageInfo<StoLockDepVo> findTotalByInivite(long memberId,String symbol, int pageNo, int pageSize, String startTime, String endTime) {
        Page<StoLockDepVo> page = PageHelper.startPage(pageNo, pageSize);
        lockMarketRewardDetailMapper.findDepByInviterAsTotal(memberId, symbol,startTime, endTime);
        return page.toPageInfo();
    }

    /**
     * 查询用户子部门总业绩
     * @param memberId
     * @param startTime
     * @param endTime
     * @return
     */
    public BigDecimal getSubPerformanceAmountById(Long memberId, String startTime,String endTime, String symbol) {
        return lockMarketRewardDetailMapper.findSubPerformanceAmountById(memberId, startTime, endTime, symbol);
    }
}
