package com.spark.bitrade.jobhandler;

import com.spark.bitrade.entity.MemberPermissionsRelieveTask;
import com.spark.bitrade.service.MemberPermissionService;
import com.spark.bitrade.service.MemberPermissionsRelieveTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 冻结权限自动解锁job
 * @author tansitao
 * @time 2018/11/29 13:58 
 */
@JobHandler(value="permissionRelieveJobHandler")
@Component
@Slf4j
public class PermissionRelieveJobHandler extends IJobHandler {

	@Autowired
	private MemberPermissionsRelieveTaskService mprTaskService;
	@Autowired
	private MemberPermissionService memberPermissionService;
	@Override
	public ReturnT<String> execute(String param) throws Exception {
		//解锁锁仓活动中满足解锁条件的锁仓记录
		XxlJobLogger.log("======自动解冻权限任务job, 开始======");
		List<MemberPermissionsRelieveTask> mprTaskList= mprTaskService.findAllPermissionsRelieveTask();
		if (mprTaskList != null && mprTaskList.size() > 0){
			XxlJobLogger.log("======需要解冻的权限任务数量:" +  mprTaskList.size() + "======");
			for (MemberPermissionsRelieveTask mprTask:mprTaskList) {
				//权限限制操作
				try {
					memberPermissionService.relievePermission(mprTask);
				}catch (Exception e){
					log.error("======处理解冻任务失败ID:" +  mprTask.getId() + "======", e);
					XxlJobLogger.log("======处理解冻任务失败ID:" +  mprTask.getId() + "======");
				}
			}

		}else{
			XxlJobLogger.log("======无需要解冻的权限任务======");
		}
		XxlJobLogger.log("======自动解冻权限任务job, 完毕======");
		return SUCCESS;
	}

}
