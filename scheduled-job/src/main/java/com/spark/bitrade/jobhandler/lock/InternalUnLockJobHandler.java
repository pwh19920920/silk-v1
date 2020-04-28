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
  * 内部锁仓解锁
  * @author tansitao
  * @time 2018/8/7 10:35 
  */
@JobHandler(value="internalUnLockJobHandler")
@Component
public class InternalUnLockJobHandler extends IJobHandler {

	@Autowired
	private LockConfig lockConfig;

	@Autowired
	private ILockDetailService lockDetailService;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		//解锁锁仓活动中满足解锁条件的锁仓记录
		XxlJobLogger.log("======内部锁仓活动解锁job, 开始解锁======");
		lockDetailService.unlockFinanActivity(lockConfig.getUnlockNum());
		XxlJobLogger.log("======内部锁仓活动解锁job, 解锁完毕======");
		return SUCCESS;
	}

}
