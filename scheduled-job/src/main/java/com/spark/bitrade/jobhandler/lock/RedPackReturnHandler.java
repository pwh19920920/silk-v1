package com.spark.bitrade.jobhandler.lock;

import com.spark.bitrade.entity.RedPackManage;
import com.spark.bitrade.entity.RedPackReceiveRecord;
import com.spark.bitrade.service.RedPackManageService;
import com.spark.bitrade.service.RedPackReceiveRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.11.26 16:04  
 */
@JobHandler(value = "redPackReturnHandler")
@Component
public class RedPackReturnHandler extends IJobHandler {

    @Autowired
    private RedPackReceiveRecordService recordService;
    @Autowired
    private RedPackManageService redPackManageService;
    @Override
    public ReturnT<String> execute(String batchNum) throws Exception {
        XxlJobLogger.log("==================红包退回开始==================");
        //红包退回
        List<RedPackReceiveRecord> list = recordService.returnRedPack();
        for (RedPackReceiveRecord record:list){
            try {
                recordService.doReturn(record);
            }catch (Exception e){
                XxlJobLogger.log("==================退回红包失败ID:"+record.getId()+"==================");
                XxlJobLogger.log(e.getMessage());
            }
        }
        //红包配置过期退回 冻结余额
        List<RedPackManage> manages = redPackManageService.findExpireManages();
        for (RedPackManage manage:manages){
            try {
                redPackManageService.returnManage(manage);
            }catch (Exception e){
                XxlJobLogger.log("==================退回红包配置到总帐户失败ID:"+manage.getId()+"==================");
                XxlJobLogger.log(e.getMessage());
            }
        }
        return ReturnT.SUCCESS;
    }
}

