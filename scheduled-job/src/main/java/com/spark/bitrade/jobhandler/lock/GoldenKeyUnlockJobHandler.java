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
 * 金钥匙活动，bttc 锁仓返还
 * @author dengdy
 * @time 2019/4/17
 */
@JobHandler(value="goldenKeyUnlockJobHandler")
@Component
public class GoldenKeyUnlockJobHandler extends IJobHandler {
    @Autowired
    private LockConfig lockConfig;

    @Autowired
    private ILockDetailService lockDetailService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //解锁推荐人分期收益记录中满足解锁条件的记录
        XxlJobLogger.log("======金钥匙活动，bttc返还job, 开始解锁======");
        lockDetailService.unlockGoldenKeyPrincipal(lockConfig.getUnlockNum());
        XxlJobLogger.log("======IEO活动，bttc返还job, 解锁完毕======");
        return SUCCESS;
    }
}
