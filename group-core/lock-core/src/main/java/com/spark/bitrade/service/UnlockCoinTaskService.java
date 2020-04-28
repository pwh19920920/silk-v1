package com.spark.bitrade.service;

import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.dao.UnlockCoinTaskDao;
import com.spark.bitrade.entity.UnlockCoinTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/***
 * 锁仓活动解锁任务
 * @author yangch
 * @time 2018.08.02 12:00
 */

@Service
public class UnlockCoinTaskService {
    @Autowired
    private UnlockCoinTaskDao unlockCoinTaskDao;

    public UnlockCoinTask save(UnlockCoinTask unlockCoinTask) {
        return unlockCoinTaskDao.saveAndFlush(unlockCoinTask);
    }

    /**
     * 查询最新的一条解锁任务
     * @param refActivitieId 活动ID
     * @return
     */
    public UnlockCoinTask findOneNewly(Long refActivitieId) {
        return unlockCoinTaskDao.findFirstByRefActivitieIdOrderByIdDesc(refActivitieId);
    }

    /**
      * 更新解锁任务状态
      * @author tansitao
      * @time 2018/8/3 11:05 
      */
    @Transactional
    public int updateUnlockCoinTaskStatus( long id, ProcessStatus status, ProcessStatus oldStatus){
        return  unlockCoinTaskDao.updateUnlockCoinTaskStatus(id, status, oldStatus);
    }


}
