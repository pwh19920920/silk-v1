package com.spark.bitrade.service.otc.impl;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.Appeal;
import com.spark.bitrade.entity.SysNotificationChannelConfig;
import com.spark.bitrade.entity.SysNotificationTemplate;
import com.spark.bitrade.constant.AppealStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.constant.NotificationType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.otc.IAppealOrderCompletedEnventHandler;
import com.spark.bitrade.service.AppealService;
import com.spark.bitrade.service.CommonSilkPlatInformationService;
import com.spark.bitrade.service.KafkaDelayService;
import com.spark.bitrade.service.MessageDealService;
import com.spark.bitrade.service.*;
import com.spark.bitrade.service.impl.SysNotificationChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * C2C订单申诉完成事件通知处理实现类
 *
 * @authortansitao
 * @time2018/12/2410:32
 */
@Service
@Slf4j
public class AppealOrderCompleteMessageHandlerImpl implements IAppealOrderCompletedEnventHandler {
    @Autowired
    private SysNotificationChannelConfigService sysNotificationChannelConfigService;
    @Autowired
    private MessageDealService messageDealService;
    @Autowired
    private AppealService appealService;
    @Autowired
    private CommonSilkPlatInformationService commonSilkPlatInformationService;
    @Autowired
    private KafkaDelayService kafkaDelayService;

    @Autowired
    private MonitorRuleCheckService monitorRuleCheckService;
    @Autowired
    private MonitorRuleService monitorRuleService;
    @Autowired
    private MemberService memberService;


    @Override
    public void handle(CollectCarrier carrier) {
        String refId = carrier.getRefId();
        log.info("================={}=================appealId:{}", carrier.getCollectType().getCnName(), refId);
        Appeal appeal = appealService.findOne(Long.parseLong(refId));
        if (appeal == null || appeal.getStatus() != AppealStatus.PROCESSED) {
            log.info("==============申诉记录不存在，或者不是处理完成申诉事件===================appealId:{}====memberId:{},appeal===={}", carrier.getRefId(), carrier.getMemberId(), appeal);
            // 重新写入kafka，直到成功消费
            kafkaDelayService.sendOtcAppealOrderComplete(refId);
            return;
        }

        List<SysNotificationChannelConfig> sncConfigList = sysNotificationChannelConfigService.findByType(MonitorTriggerEvent.OTC_APPEAL_COMPLETE.getOrdinal());
        if (sncConfigList != null && sncConfigList.size() > 0) {
            for (SysNotificationChannelConfig sysNotificationChannelConfig : sncConfigList) {
                // 获取订单申诉完成系统消息内容模板
                SysNotificationTemplate sysNotificationTemplate = messageDealService.getSystemMessage(carrier, sysNotificationChannelConfig.getType(), NotificationType.SYSTEM);
                // 判断模板类容是否为空
                if (sysNotificationTemplate != null) {
                    // 判断是否推送系统消息
                    if (sysNotificationChannelConfig.getIsSystem() == BooleanEnum.IS_TRUE) {
                        messageDealService.pushOrderAppealSuccessMessage(appeal, sysNotificationTemplate, appeal.getOrder());
                    }
                }
            }
        } else {
            log.info("============系统消息推送，无{}配置，订单id：{}============", MonitorTriggerEvent.OTC_APPEAL_COMPLETE.getCnName(), carrier.getRefId());
        }

        // 发送邮件
        Long successId = appeal.getInitiatorId();
        if (appeal.getStatus() == AppealStatus.PROCESSED && appeal.getIsSuccess() == BooleanEnum.IS_FALSE) {
            // 胜诉方
            successId = appeal.getAssociateId();
        }
        // 推送胜诉邮件 1SELL-出售，customer_id:买方，member_id：卖方，反之。
        String country = commonSilkPlatInformationService.getCountry(successId);
        if (country != null && !"".equals(country)) {
            Map<String, Object> map = commonSilkPlatInformationService.getAppealCompleteOrderModel(appeal.getOrder());
            commonSilkPlatInformationService.sendCommonNotice(SysConstant.NO_LIMITATION, successId, InfoType.OTC_APPEAL_WIN, country, map, appeal.getOrder().getOrderSn(), NoticeType.SYS_NOTICE_OTC_ORDER);
            commonSilkPlatInformationService.sendCommonEmail(SysConstant.NO_LIMITATION, successId, InfoType.OTC_APPEAL_WIN, country, map);
        }

        log.info("==========================================申诉权限冻结检查开始======================================================");
        Member member = memberService.findOne(Long.parseLong(carrier.getMemberId()));
        // 查询出所有的订单申诉告警规则
        List<MonitorRuleConfig> monitorRuleConfigList = monitorRuleService.findAllByType(MonitorTriggerEvent.OTC_APPEAL_COMPLETE);
        if (monitorRuleConfigList != null && monitorRuleConfigList.size() > 0) {
            // 遍历告警规则，对比取消次数是否满足告警,对比用户级别
            for (MonitorRuleConfig monitorRuleConfig : monitorRuleConfigList) {
                if (member.getMemberLevel() == monitorRuleConfig.getTriggerUserLevel()) {
                    // 如果满足告警规则，冻结相应的权限
                    if (!monitorRuleCheckService.checkRule(monitorRuleConfig, carrier)) {
                        log.info("===============用户{}满足告警规则{},===将{}====，禁止时间{}天=============", carrier.getMemberId(), monitorRuleConfig.getId(), monitorRuleConfig.getExecuteEvent().getCnName(), monitorRuleConfig.getExecuteDuration() / 60 / 24);
                    } else {
                        log.info("===============用户{}触发了告警{}，次数不足，不进行权限冻结=============", carrier.getMemberId(), monitorRuleConfig.getId());
                    }
                }
            }
        }
        log.info("==========================================申诉权限冻结检查结束======================================================");
    }
}