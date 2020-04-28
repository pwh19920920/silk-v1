package com.spark.bitrade.jobhandler.lock;

import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.service.ILockUttBizService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.11.22 17:41  
 */
@JobHandler(value="lockInnovativeReleasePlanHandler")
@Component
public class LockInnovativeReleasePlanHandler extends IJobHandler {

    @Autowired
    private ILockUttBizService lockUttBizService;


    @Override
    public ReturnT<String> execute(String batchNum) throws Exception {
        lockUttBizService.releaseUtt(TransactionType.RELEASE_BTLF);
        return ReturnT.SUCCESS;
    }
}
