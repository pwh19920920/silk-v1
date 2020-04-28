package com.spark.bitrade.controller.alarm;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.entity.AlarmMonitor;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.service.AlarmMonitorService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 告警监控
 * @author Zhang Yanjun
 * @time 2018.09.27 11:25
 */
@RestController
@RequestMapping("alarm")
@Slf4j
public class AlarmMonitorController extends BaseController {

    @Autowired
    AlarmMonitorService alarmMonitorService;
    @Autowired
    MemberService memberService;


    /**
     * 告警监控分页
     * @author Zhang Yanjun
     * @time 2018.09.28 10:51
     * @param memberId  告警会员id
     * @param alarmType  告警类型
     * @param pageNo
     * @param pageSize
     * @param pageSize
     */
    @PostMapping("page-query")
    @RequiresPermissions("monitor:alarm-page-query")
    public MessageResult page(Long memberId,Long alarmType,int pageNo, int pageSize){
        PageInfo<AlarmMonitor> pageInfo=alarmMonitorService.findAllBy(memberId,alarmType,pageNo, pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 告警处理
     * @author Zhang Yanjun
     * @time 2018.09.28 10:51
     * @param maintenanceMsg  处理意见
     * @param id  告警id
     * @param admin  处理人
     */
    @PostMapping("handle")
    @RequiresPermissions("monitor:alarm-handle")
    public MessageResult alarmHandle(String maintenanceMsg,Long id,@SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin){
        Assert.notNull(id,"请输入id");
        AlarmMonitor alarmMonitor=alarmMonitorService.findOneById(id);
        Assert.notNull(alarmMonitor,"无该条告警记录");
        Assert.notNull(alarmMonitor.getMemberId(),"该条告警记录无会员id");
        Member member=memberService.findOne(alarmMonitor.getMemberId());
        Assert.notNull(member,"无该会员");
        alarmMonitor.setMaintenanceMsg(maintenanceMsg);
        alarmMonitor.setMaintenanceId(admin.getId());
        alarmMonitor.setStatus(BooleanEnum.IS_TRUE);
        alarmMonitorService.save(alarmMonitor);
        return success(alarmMonitor);
    }

}
