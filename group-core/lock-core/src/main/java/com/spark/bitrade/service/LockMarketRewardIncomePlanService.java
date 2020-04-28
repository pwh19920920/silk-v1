package com.spark.bitrade.service;

import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.Symbol;
import com.spark.bitrade.dao.LockMarketRewardIncomePlanDao;
import com.spark.bitrade.entity.LockMarketRewardIncomePlan;
import com.spark.bitrade.mapper.dao.LockMarketRewardIncomePlanMapper;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.vo.StoMemberInfoVo;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 市场奖励返还计划service
 * @author Zhang Yanjun
 * @time 2018.12.03 20:21
 */
@Service
public class LockMarketRewardIncomePlanService {
    @Autowired
    LockMarketRewardIncomePlanDao lockMarketRewardIncomePlanDao;
    @Autowired
    LockMarketRewardIncomePlanMapper lockMarketRewardIncomePlanMapper;

    /**
     * 新增
     * @author Zhang Yanjun
     * @time 2018.12.03 20:24
     * @param lockMarketRewardIncomePlan
     */
    @CacheEvict(cacheNames = "lockMarketRewardIncomePlan", key = "'entity:lockMarketRewardIncomePlan:'+#lockMarketRewardIncomePlan.getId()")
    public void save (LockMarketRewardIncomePlan lockMarketRewardIncomePlan){
        lockMarketRewardIncomePlanDao.save(lockMarketRewardIncomePlan);
    }

    @CacheEvict(cacheNames = "lockMarketRewardIncomePlan", key = "'entity:lockMarketRewardIncomePlan:'+#lockMarketRewardIncomePlan.getId()")
    public LockMarketRewardIncomePlan saveToReturn (LockMarketRewardIncomePlan lockMarketRewardIncomePlan){
        return lockMarketRewardIncomePlanDao.save(lockMarketRewardIncomePlan);
    }

    /**
     * 更新状态
     * @author Zhang Yanjun
     * @time 2018.12.03 20:11
     * @param id
     * @param status
     */
    @CacheEvict(cacheNames = "lockMarketRewardIncomePlan", key = "'entity:lockMarketRewardIncomePlan:'+#id")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, LockBackStatus newStatus, LockBackStatus oldStatus){
        return lockMarketRewardIncomePlanDao.updateStatus(id, newStatus, oldStatus) > 0 ? true : false;
    }

    /**
     * 待返还列表
     * @author Zhang Yanjun
     * @time 2018.12.03 20:17
     * @param
     */
    @Cacheable(cacheNames = "lockMarketRewardIncomePlan", key = "'entity:lockMarketRewardIncomePlan:list'")
    @ReadDataSource
    public List<LockMarketRewardIncomePlan> findAllByBack(){
        return lockMarketRewardIncomePlanMapper.findAllByBack();
    }

    /**
     * 根据id查询
     * @author Zhang Yanjun
     * @time 2018.12.03 20:19
     * @param id
     */
    @Cacheable(cacheNames = "lockMarketRewardIncomePlan", key = "'entity:lockMarketRewardIncomePlan:'+#id")
    @ReadDataSource
    public LockMarketRewardIncomePlan findOneById(Long id){
        return lockMarketRewardIncomePlanMapper.findOneById(id);
    }

    /**
     * 用户到账奖励
     * @author Zhang Yanjun
     * @time 2018.12.04 17:29
     * @param memberId
     */
    @ReadDataSource
    public StoMemberInfoVo findAllByBacked(Long memberId,String symbol, String startTime, String endTime){
        List<Map<String,Object>> list = lockMarketRewardIncomePlanMapper.findAllByBacked(memberId, symbol,startTime, endTime);
        StoMemberInfoVo stoMemberInfoVo = new StoMemberInfoVo();
        for (int i=0; i<list.size(); i++){
            String rewardType=list.get(i).get("rewardType").toString();
            String backAmount=list.get(i).get("backAmount").toString();
            BigDecimal amount= new BigDecimal( backAmount );
            if (rewardType.equals("0")){
                stoMemberInfoVo.setReferrerArrived(amount);
            }else if (rewardType.equals("1")){
                stoMemberInfoVo.setCrossArrived(amount);
            }else {
                stoMemberInfoVo.setTrainingArrived(amount);
            }
        }
        return stoMemberInfoVo;
    }

    /**
     * 用户总奖励
     * @author Zhang Yanjun
     * @time 2018.12.04 17:47
     * @param memberId
     */
    @ReadDataSource
    public StoMemberInfoVo findAllReward(Long memberId,String symbol, String startTime, String endTime){
        List<Map<String,Object>> list = lockMarketRewardIncomePlanMapper.findAllReward(memberId, symbol,startTime, endTime);
        StoMemberInfoVo stoMemberInfoVo = new StoMemberInfoVo();
        for (int i=0; i<list.size(); i++){
            String rewardType=list.get(i).get("rewardType").toString();
            String amountTotal=list.get(i).get("amountTotal").toString();
            BigDecimal amount= new BigDecimal( amountTotal);
            if (rewardType.equals("0")){
                stoMemberInfoVo.setReferrerAmount(amount);
            }else if (rewardType.equals("1")){
                stoMemberInfoVo.setCrossAmount(amount);
            }else {
                stoMemberInfoVo.setTrainingAmount(amount);
            }
        }
        return stoMemberInfoVo;
    }

    /**
     * 查询实时返佣的数据
     * @author tansitao
     * @time 2018/12/7 10:09 
     */
    @WriteDataSource
    public LockMarketRewardIncomePlan findOneByDetailIdAndMemberId(Long marketRewardDetailId, Long memberId, String rewardTime, LockBackStatus status) {
        return lockMarketRewardIncomePlanMapper.findOneByDetailIdAndMemberId(marketRewardDetailId, memberId, rewardTime, status.getOrdinal());
    }

    /**
     * 查询可返佣的数据
     * @author tansitao
     * @time 2018/12/28 16:48 
     */
    @ReadDataSource
    public List<LockMarketRewardIncomePlan> findCanUnLockList(Date rewardTime, LockBackStatus status, int lockNum) {
        return lockMarketRewardIncomePlanMapper.findCanUnLockList(status.getOrdinal(), lockNum, rewardTime);
    }
}
