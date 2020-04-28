package com.spark.bitrade.jobhandler.lock;


import com.spark.bitrade.service.ILockService;
import com.spark.bitrade.util.MessageRespResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;


/**
 * @author fumy
 * @time 2019.03.19 14:22
 */
@JobHandler(value="STOMissRewardJobHandler")
@Component
@Slf4j
public class STOMissRewardJobHandler extends IJobHandler{

    @Autowired
    private ILockService lockService;


    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("======STO锁仓活动丢失返佣数据补充job, 开始查询======");

        MessageRespResult respResult = lockService.rewardRedo(param);
        XxlJobLogger.log(respResult.getMessage());

        XxlJobLogger.log("======STO锁仓活动丢失返佣数据补充job, 查询完毕======");
        return SUCCESS;
    }


}
