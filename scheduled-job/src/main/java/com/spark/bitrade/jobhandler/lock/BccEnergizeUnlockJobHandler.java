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
 * BCC 赋能计划，BCC 锁仓分期返还
 * @author fatKarin
 * @time 2019/7/1
 */
@JobHandler(value="BccEnergizeUnlockJobHandler")
@Component
public class BccEnergizeUnlockJobHandler extends IJobHandler{

    @Autowired
    private LockConfig lockConfig;

    @Autowired
    private ILockDetailService lockDetailService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //解锁推荐人分期收益记录中满足解锁条件的记录
        XxlJobLogger.log("======BCC赋能活动，活动币种分期返还job, 开始解锁======");
        lockDetailService.unlockBccEnergize(lockConfig.getUnlockNum());
        XxlJobLogger.log("======BCC赋能活动，活动币种返还job, 解锁完毕======");
        return SUCCESS;
    }
}
