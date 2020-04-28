package com.spark.bitrade.service.otc.impl;

import com.alibaba.druid.util.StringUtils;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MonitorRuleConfig;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.MonitorRuleCheckService;
import com.spark.bitrade.service.MonitorRuleService;
import com.spark.bitrade.envent.otc.ICancelOrderEnventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 *C2C撤销订单事件通知处理实现类
 *@authoryangch
 *@time2018.11.0215:23
 */
@Service
@Slf4j
public class CancelOrderCheckerImpl implements ICancelOrderEnventHandler {
    @Autowired
    private MonitorRuleService monitorRuleService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MonitorRuleCheckService monitorRuleCheckService;

    @Override
    public void handle(CollectCarrier carrier) {
        log.info("=================CancelOrderCheckerImpl=================refId:" + carrier.getRefId() + "=================memberId:" + carrier.getMemberId());
        if (StringUtils.isEmpty(carrier.getMemberId())) {
            log.info("==============会员id不存在，无法进行取消订单监控===================");
            return;
        }

        Member member = memberService.findOne(Long.parseLong(carrier.getMemberId()));
        // 查询出所有的取消订单告警规则
        List<MonitorRuleConfig> monitorRuleConfigList = monitorRuleService.findAllByType(MonitorTriggerEvent.OTC_CANCEL_ORDER);
        if (monitorRuleConfigList != null && monitorRuleConfigList.size() > 0) {
            // 遍历告警规则，对比取消次数是否满足告警,对比用户级别
            for (MonitorRuleConfig monitorRuleConfig : monitorRuleConfigList) {
                if (member.getMemberLevel() == monitorRuleConfig.getTriggerUserLevel()) {
                    // 如果满足告警规则，冻结相应的权限
                    if (!monitorRuleCheckService.checkRule(monitorRuleConfig, carrier)) {
                        log.info("===============用户{}触发了告警规则{},===将{}====，时间{}=============", carrier.getMemberId(), monitorRuleConfig.getId(), monitorRuleConfig.getExecuteEvent().getCnName(), monitorRuleConfig.getExecuteDuration());
                    } else {
                        log.info("===============用户{}触发了告警{}，次数不足，不进行权限冻结=============", carrier.getMemberId(), monitorRuleConfig.getId());
                    }
                }
            }
        }
    }
}