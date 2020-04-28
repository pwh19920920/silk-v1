package com.spark.bitrade.jobhandler;

import com.spark.bitrade.entity.TechRechargeRecord;
import com.spark.bitrade.service.ITechRechargeRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fumy
 * @time 2018.06.20 15:49
 */
@JobHandler(value = "techRechargeHandler")
@Component
public class TechRechargeHandler extends IJobHandler {

    @Autowired
    ITechRechargeRecordService iTechRechargeRecordService;

    private Logger logger = LoggerFactory.getLogger(TechRechargeHandler.class);

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //核心处理逻辑描述:
        //1.根据操作类型（type）分类 查询member_transcation表， 查询出 技术充（减）币数据列表
        //2.循环将列表的数据逐条插入到技术充（减）币数据表

        boolean isSucc = techRechargeRecord();//技术充（减）币数据列表查询
        if(isSucc){
            XxlJobLogger.log("技术充（减）币数据生成完毕!>......................");
        }else {
            XxlJobLogger.log("技术充（减）币数据生成失败!>......................");
        }
        return SUCCESS;
    }

    public boolean techRechargeRecord(){

        List<TechRechargeRecord> list = iTechRechargeRecordService.techRechargeList();

        int row=0;
        if(list!=null && list.size()> 0){
            logger.info("开始生成技术充（减）币数据..........");
            row = iTechRechargeRecordService.insertRecord(list);
        }

        return row > 0 ? true : false;
    }
}
