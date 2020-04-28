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
  * CNYT增值计划，解锁推荐人分期收益
  * @author tansitao
  * @time 2018/12/28 13:52 
  */
@JobHandler(value="cnytRewardIncomeUnLockJobHandler")
@Component
public class CnytRewardIncomeUnLockJobHandler extends IJobHandler {

	@Autowired
	private LockConfig lockConfig;

	@Autowired
	private ILockDetailService lockDetailService;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		//解锁推荐人分期收益记录中满足解锁条件的记录
		XxlJobLogger.log("======CNYT增值计划，解锁推荐人分期收益job, 开始解锁======");
		lockDetailService.unlockCnytRewardIncome(lockConfig.getUnlockNum());
		XxlJobLogger.log("======CNYT增值计划，解锁推荐人分期收益job, 解锁完毕======");
		return SUCCESS;
	}

}
