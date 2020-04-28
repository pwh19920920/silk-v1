package com.spark.bitrade.service;

import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.constant.BettingStateOperateType;
import com.spark.bitrade.dao.BettingStateDao;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/***
 * 
 * @author yangch
 * @time 2018.09.14 11:14
 */

@Service
public class BettingStateService {
    @Autowired
    private BettingStateDao bettingStateDao;

    @CacheEvict(cacheNames = "bettingState", key = "'entity:bettingState:'+#bean.periodId+'-'+#bean.operate.getCode()")
    @Transactional(rollbackFor = Exception.class)
    public BettingState save(BettingState bean){
        return bettingStateDao.saveAndFlush(bean);
    }

    @Cacheable(cacheNames = "bettingState", key = "'entity:bettingState:'+#periodId+'-'+#operate.getCode()")
    public BettingState findOne(Long periodId, BettingStateOperateType operate){
        return bettingStateDao.findBettingStateByPeriodIdAndOperate(periodId, operate);
    }

    /**
     * 查询
     * @param periodId 期数id
     * @param opType 操作类型
     * @return
     */
    @Cacheable(cacheNames = "bettingState", key = "'entity:bettingState:'+#periodId+'-'+#opType.getCode()")
    public BettingState findBettingState(Long periodId,
                                           BettingStateOperateType opType){
        BettingState bettingState =
                getService().findOne(periodId, opType);
        if(null == bettingState) {
            //创建记录
            bettingState = new BettingState();
            bettingState.setPeriodId(periodId);
            bettingState.setOperate(opType);
            bettingState.setMark(BettingStateOperateMark.UNTREATED);

            return getService().save(bettingState);
        }

        return bettingState;
    }

    public BettingStateService getService(){
        return SpringContextUtil.getBean(BettingStateService.class);
    }

}
