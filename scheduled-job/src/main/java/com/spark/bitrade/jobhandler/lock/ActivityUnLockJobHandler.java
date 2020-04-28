package com.spark.bitrade.jobhandler.lock;

import com.spark.bitrade.config.LockConfig;
import com.spark.bitrade.service.ILockDetailService;
import com.spark.bitrade.util.RPCUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
  * 活动锁仓解锁job
  * @author tansitao
  * @time 2018/7/19 10:15 
  */
@JobHandler(value="activityUnLockJobHandler")
@Component
public class ActivityUnLockJobHandler extends IJobHandler {

	@Autowired
	private LockConfig lockConfig;

	@Autowired
	private ILockDetailService lockDetailService;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		//解锁锁仓活动中满足解锁条件的锁仓记录
		XxlJobLogger.log("======锁仓活动解锁job, 开始解锁======");
		lockDetailService.unlockActivityLock(lockConfig.getUnlockNum());
		XxlJobLogger.log("======锁仓活动解锁job, 解锁完毕======");
		return SUCCESS;
	}

}
