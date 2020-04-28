package com.spark.bitrade.service.uc.impl;

import com.spark.bitrade.constant.MonitorExecuteEnvent;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberPermissionsRelieveTask;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.uc.IChangePhoneEmailEnventHandler;
import com.spark.bitrade.service.MemberPermissionsRelieveTaskService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.MonitorRuleCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.10.21 15:17  
 */
@Service
@Slf4j
public class ChangePhoneEmailHandlerImpl implements IChangePhoneEmailEnventHandler {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberPermissionsRelieveTaskService mprTaskService;
    @Autowired
    private MonitorRuleCheckService monitorRuleCheckService;
    @Override
    public void handle(CollectCarrier carrier) {
        //创建一个定时解冻任务
        log.info("=======================触发修改手机邮箱事件禁止交易24小时memberId:{}========================",carrier.getMemberId());
        Member member = memberService.findOne(Long.parseLong(carrier.getMemberId()));
        String refId = carrier.getRefId();
        List<MemberPermissionsRelieveTask> tasks=mprTaskService.findListByMemberIdAndPermissionsType(member.getId(),MonitorExecuteEnvent.FORBID_EMAIL_TRANSACTIONS_STATUS,
                MonitorExecuteEnvent.FORBID_MOBILE_TRANSACTIONS_STATUS);
        if("email".equals(refId)){
            monitorRuleCheckService.excuteChangePhoneEmailTask(member,tasks,MonitorExecuteEnvent.FORBID_EMAIL_TRANSACTIONS_STATUS);
        }else if("mobile".equals(refId)){
            monitorRuleCheckService.excuteChangePhoneEmailTask(member,tasks,MonitorExecuteEnvent.FORBID_MOBILE_TRANSACTIONS_STATUS);
        }
        log.info("===========================================处理成功===============================================================");
    }




}
