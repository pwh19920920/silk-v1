package com.spark.bitrade.service.exchange.impl;

import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MonitorRuleConfig;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.exchange.ICancelOrderEnventHandler;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.MonitorRuleCheckService;
import com.spark.bitrade.service.MonitorRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 *  币币交易撤单触发 警告规则
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.23 16:38  
 */
@Service
@Slf4j
public class CancelOrderMessageHandlerImpl implements ICancelOrderEnventHandler {

    @Autowired
    private MonitorRuleCheckService monitorRuleCheckService;
    @Autowired
    private MonitorRuleService monitorRuleService;
    @Autowired
    private MemberService memberService;

    @Override
    public void handle(CollectCarrier carrier) {
        log.info("===================进入币币交易撤单触发==================");
        Member member = memberService.findOne(Long.parseLong(carrier.getMemberId()));
        //查询出所有的订单申述告警规则
        List<MonitorRuleConfig> monitorRuleConfigList = monitorRuleService.findAllByType(MonitorTriggerEvent.EXCHANGE_CANCEL_ORDER);
        if (!CollectionUtils.isEmpty(monitorRuleConfigList)) {
            //遍历告警规则，对比取消次数是否满足告警,对比用户级别
            for (MonitorRuleConfig config : monitorRuleConfigList) {
                if (member.getMemberLevel() == config.getTriggerUserLevel()) {
                    //如果满足告警规则，冻结相应的权限
                    if(!monitorRuleCheckService.checkRule(config,carrier)){
                        log.info("===============用户{}满足告警规则{},===将{}====，禁止时间{}天=============", carrier.getMemberId(), config.getId(), config.getExecuteEvent().getCnName(), config.getExecuteDuration()/60/24);
                    }
                    else{
                        log.info("===============用户{}触发了告警{}，次数不足，不进行权限冻结=============",carrier.getMemberId(), config.getId());
                    }
                }
            }
        }
    }
}
