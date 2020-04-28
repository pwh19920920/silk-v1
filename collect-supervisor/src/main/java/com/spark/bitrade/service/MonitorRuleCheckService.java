package com.spark.bitrade.service;

import com.spark.bitrade.constant.AlarmType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.constant.MonitorExecuteEnvent;
import com.spark.bitrade.entity.AlarmMonitor;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberPermissionsRelieveTask;
import com.spark.bitrade.entity.MonitorRuleConfig;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/***
 * 
 * @author yangch
 * @time 2018.11.05 10:25
 */
@Service
@Slf4j
public class MonitorRuleCheckService {
    @Autowired
    private RedisCountorService redisCountorService;
    @Autowired
    private AlarmMonitorService alarmMonitorService;
    @Autowired
    private MemberPermissionService memberPermissionService;
    @Autowired
    private MemberPermissionsRelieveTaskService mprTaskService;

    public long increment(final String key, final long timeout){
        return redisCountorService.increment(key, timeout, TimeUnit.MINUTES);
    }

    /**
     * 生成用户的监控ID
     * @param ruleConfig
     * @param carrier
     * @return
     */
    public String getMonitorRuleKey(MonitorRuleConfig ruleConfig, CollectCarrier carrier){
        return new StringBuilder("monitor:ruleConfig:").append(ruleConfig.getId()).append(":").append(carrier.getMemberId()).toString();
    }

    /**
     * 规则校验
     * @param ruleConfig 配置的规则
     * @param carrier
     * @return true=满足规则/false=不满足规则
     */
    public boolean checkRule(MonitorRuleConfig ruleConfig, CollectCarrier carrier) {
        String key = getMonitorRuleKey(ruleConfig, carrier);
        long count = increment(key, ruleConfig.getTriggerStageCycle());
        if(count >= ruleConfig.getTriggerTimes()) { //add by tansitao 时间： 2018/11/13 原因：修改处理逻辑
            //if(count - ruleConfig.getTriggerTimes() < 3) {  //避免频繁
            if(count - ruleConfig.getTriggerTimes() >= 0) {//add by tansitao 时间： 2018/11/13 原因：修改触发条件
                //触发规则
                try {
                    getService().executeRestrain(ruleConfig, carrier);
                }catch (Exception e){
                    log.error(ExceptionUtils.getFullStackTrace(e));
                    log.error("规则触发失败ID:{}",ruleConfig.getId());
                }
            }
            log.warn("{}用户触发规则，规定id={}", carrier.getMemberId(), ruleConfig.getId());
            return false;
        } else {
            return true;
        }
    }

    public MonitorRuleCheckService getService(){
        return SpringContextUtil.getBean(MonitorRuleCheckService.class);
    }

    /**
     * 执行约束
     * @param ruleConfig 约束规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeRestrain(MonitorRuleConfig ruleConfig, CollectCarrier carrier){
        log.info("-------执行约束-------------"+ruleConfig);

        if(!StringUtils.isEmpty(carrier.getMemberId())) {
            long memberId =  Long.parseLong(carrier.getMemberId());
            //生成监控数据
            String msg = carrier.getMemberId()+"用户"+ruleConfig.getTriggerStageCycle()+"分钟内‘"
                    +ruleConfig.getTriggerEvent().getCnName()+"’超过"+ruleConfig.getTriggerTimes()
                    +"次，触发‘"+ruleConfig.getExecuteEvent().getCnName()+"’"
                    +ruleConfig.getExecuteDuration()+"分钟（规则ID="+ruleConfig.getId()+"）";

            AlarmMonitor alarmMonitor = new AlarmMonitor();
            alarmMonitor.setMemberId(memberId);
            alarmMonitor.setAlarmType(CollectActionEventType2AlarmType(carrier.getCollectType()));
            alarmMonitor.setAlarmMsg(msg);
            alarmMonitor.setStatus(BooleanEnum.IS_FALSE);
            //存入告警监控表
            AlarmMonitor monitor = alarmMonitorService.save(alarmMonitor);
            log.info("msg==="+msg);

            //add by tansitao 时间： 2018/11/27 原因：生成自动解禁任务
            MemberPermissionsRelieveTask mprTask = mprTaskService.findByMemberIdAndPermissionsType(memberId, ruleConfig.getExecuteEvent());
            if(mprTask != null){
                //已有等待解锁权限的任务，则在之前的冻结时间上加上新的冻结时间，并将之前的自动解冻任务设置为不生效
                MemberPermissionsRelieveTask newMprTask = new MemberPermissionsRelieveTask();
                newMprTask.setRelieveTime(DateUtil.addMinToDate(mprTask.getRelieveTime(), ruleConfig.getExecuteDuration()));
                newMprTask.setMemberId(memberId);
                newMprTask.setRelievePermissionsType(ruleConfig.getExecuteEvent());
                newMprTask.setAlarmMonitorId(monitor.getId());
                mprTaskService.save(newMprTask);

                //将之前的数据变成不可用
                mprTask.setUsable(BooleanEnum.IS_FALSE);
                mprTaskService.save(mprTask);
            }else {
                //保存一条自动解锁权限任务
                mprTask = new MemberPermissionsRelieveTask();
                mprTask.setMemberId(memberId);
                mprTask.setRelievePermissionsType(ruleConfig.getExecuteEvent());
                mprTask.setRelieveTime(DateUtil.addMinToDate(new Date(), ruleConfig.getExecuteDuration()));
                mprTask.setAlarmMonitorId(monitor.getId());
                mprTaskService.save(mprTask);
            }

            //权限限制操作
            memberPermissionService.updatePermission(memberId, ruleConfig.getExecuteEvent());
        }


    }


    @Transactional(rollbackFor = Exception.class)
    public void excuteChangePhoneEmailTask(Member member, List<MemberPermissionsRelieveTask> tasks, MonitorExecuteEnvent executeEnvent){

        if(!CollectionUtils.isEmpty(tasks)){
            //已有等待解锁权限的任务，则在之前的冻结时间上加上新的冻结时间，并将之前的自动解冻任务设置为不生效
            MemberPermissionsRelieveTask newMprTask = new MemberPermissionsRelieveTask();
            newMprTask.setRelieveTime(DateUtil.addMinToDate(new Date(),1440));
            newMprTask.setMemberId(member.getId());
            newMprTask.setRelievePermissionsType(executeEnvent);
            mprTaskService.save(newMprTask);

            //将之前的数据变成不可用
            tasks.forEach(m->{
                m.setUsable(BooleanEnum.IS_FALSE);
                mprTaskService.save(m);
            });
        }else {
            //保存一条自动解锁权限任务
            MemberPermissionsRelieveTask mprTask = new MemberPermissionsRelieveTask();
            mprTask.setMemberId(member.getId());
            mprTask.setRelievePermissionsType(executeEnvent);
            mprTask.setRelieveTime(DateUtil.addMinToDate(new Date(), 1440));
            mprTaskService.save(mprTask);
        }
        memberPermissionService.updatePermission(member,executeEnvent);



    }



    //CollectActionEventType 转换为 AlarmType类型
    public AlarmType CollectActionEventType2AlarmType(CollectActionEventType actionEventType){
        if(actionEventType.getCnType().equalsIgnoreCase("OTC")){
            //OTC类型
            switch (actionEventType){
                case OTC_APPEAL_ORDER_COMPLETE:
                    return AlarmType.OTC_APPEAL;
                case OTC_CANCEL_ORDER:
                    return AlarmType.OTC_CANCEL;
                case EXCHANGE_CANCEL_ORDER:
                    return AlarmType.CANCEL;
            }
        }
        if(actionEventType.getCnType().equalsIgnoreCase("EXCHANGE")){
            //OTC类型
            switch (actionEventType){
                case EXCHANGE_CANCEL_ORDER:
                    return AlarmType.CANCEL;
            }
        }

        return AlarmType.UNKNOWN;
    }

}
