package com.spark.bitrade.jobhandler;

import com.spark.bitrade.constant.MemberLevelEnum;
import com.spark.bitrade.dao.MemberApplicationDao;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberApplication;
import com.spark.bitrade.entity.MemberApplicationForjob;
import com.spark.bitrade.service.IMemberApplicationService;
import com.spark.bitrade.service.MemberApplicationService;
import com.spark.bitrade.service.MemberService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * @author fumy
 * @time 2018.09.07 09:51
 */
@JobHandler(value="memberApplicationJobHandler")
@Component
public class MemberAppliactionJobHandler extends IJobHandler {

    @Autowired
    IMemberApplicationService service;

    @Autowired
    MemberApplicationService memberApplicationService;

    @Autowired
    MemberService memberService;


    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("=========系统自动审核3分钟内未审核的实名认证，审核中...======================");
        memberApplyPassForRealName();
        XxlJobLogger.log("=========系统自动审核3分钟内未审核的实名认证，审核完成...======================");
        return SUCCESS;
    }

    public void memberApplyPassForRealName(){
        List<MemberApplicationForjob> list = service.getNoAuditList();
        if(list != null && list.size()>0){
            for(int i=0;i<list.size();i++){
                Member member = memberService.findOne(list.get(i).getMemberId());
                MemberApplication application = new MemberApplication();
                application.setId(list.get(i).getId());
                application.setAuditStatus(list.get(i).getAuditStatus());
                application.setCountry(list.get(i).getCountry());
                application.setIdCard(list.get(i).getIdCard());
                application.setIdentityCardImgFront(list.get(i).getIdentityCardImgFront());
                application.setIdentityCardImgInHand(list.get(i).getIdentityCardImgInHand());
                application.setIdentityCardImgReverse(list.get(i).getIdentityCardImgReverse());
                application.setRealName(list.get(i).getRealName());
                application.setRejectReason(list.get(i).getRejectReason());
                application.setAuditStatus(list.get(i).getAuditStatus());
                application.setCreateTime(list.get(i).getCreateTime());
                application.setMember(member);
                memberApplicationService.auditPass(application,1);
            }
        }
    }
}
