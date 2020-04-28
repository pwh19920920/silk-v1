package com.spark.bitrade.jobhandler.lock;

import com.spark.bitrade.config.LockConfig;
import com.spark.bitrade.service.ILockDetailService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * IEO 活动，bttc 锁仓分期返还
 * @author dengdy
 * @time 2019/4/17
 */
@JobHandler(value="ieoRestitutionUnlockJobHandler")
@Component
public class IeoRestitutionUnlockJobHandler extends IJobHandler{

    @Autowired
    private LockConfig lockConfig;

    @Autowired
    private ILockDetailService lockDetailService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //解锁推荐人分期收益记录中满足解锁条件的记录
        XxlJobLogger.log("======IEO活动，活动币种分期返还job, 开始解锁======");
        lockDetailService.unlockIeoRestitutionIncome(lockConfig.getUnlockNum());
        XxlJobLogger.log("======IEO活动，活动币种返还job, 解锁完毕======");
        return SUCCESS;
    }
}
