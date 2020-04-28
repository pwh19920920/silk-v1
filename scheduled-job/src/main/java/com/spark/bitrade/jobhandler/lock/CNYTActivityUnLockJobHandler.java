package com.spark.bitrade.jobhandler.lock;

import com.spark.bitrade.service.ILockDetailService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CNYT活动锁仓收益解锁job（自己锁仓的）
 * @author Zhang Yanjun
 * @time 2018.12.11 15:30
 */
@JobHandler(value="cnytActivityUnLockJobHandler")
@Component
public class CNYTActivityUnLockJobHandler extends IJobHandler {

    @Autowired
    private ILockDetailService lockDetailService;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        //解锁锁仓活动中满足解锁条件的锁仓记录
        XxlJobLogger.log("======CNYT锁仓活动用户锁仓收益解锁job, 开始解锁======");
        lockDetailService.unlockCNYTActivityLock();
        XxlJobLogger.log("======CNYT锁仓活动用户锁仓收益解锁job, 解锁完毕======");
        return SUCCESS;
    }
}
