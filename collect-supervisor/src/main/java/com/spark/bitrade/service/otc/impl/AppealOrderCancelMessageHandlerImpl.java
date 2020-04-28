package com.spark.bitrade.service.otc.impl;

import com.spark.bitrade.constant.AppealStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.constant.NotificationType;
import com.spark.bitrade.entity.Appeal;
import com.spark.bitrade.entity.SysNotificationChannelConfig;
import com.spark.bitrade.entity.SysNotificationTemplate;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.otc.IAppealOrderEnventHandler;
import com.spark.bitrade.service.AppealService;
import com.spark.bitrade.service.MessageDealService;
import com.spark.bitrade.service.impl.SysNotificationChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * C2C订单申诉取消事件通知处理实现类
 *
 * @authortansitao
 * @time2018/12/2410:32
 */
@Service
@Slf4j
public class AppealOrderCancelMessageHandlerImpl implements IAppealOrderEnventHandler {
    @Autowired
    private SysNotificationChannelConfigService sysNotificationChannelConfigService;
    @Autowired
    private MessageDealService messageDealService;
    @Autowired
    private AppealService appealService;

    @Override
    public void handle(CollectCarrier carrier) {
        log.info("================={}=================appealId:{}=================memberId:{}", carrier.getCollectType().getCnName(), carrier.getRefId(), carrier.getMemberId());
        Appeal appeal = appealService.findOne(Long.parseLong(carrier.getRefId()));
        if (appeal == null || appeal.getStatus() != AppealStatus.CANCELED) {
            log.info("==============申诉记录不存在，或者不是取消申诉事件===================appealId:{}====memberId:{}", carrier.getRefId(), carrier.getMemberId());
            return;
        }

        List<SysNotificationChannelConfig> sncConfigList = sysNotificationChannelConfigService.findByType(MonitorTriggerEvent.OTC_APPEAL_CANCEL.getOrdinal());
        if (sncConfigList != null && sncConfigList.size() > 0) {
            for (SysNotificationChannelConfig sysNotificationChannelConfig : sncConfigList) {
                // 获取订单取消申诉系统消息内容模板
                SysNotificationTemplate sysNotificationTemplate = messageDealService.getSystemMessage(carrier, sysNotificationChannelConfig.getType(), NotificationType.SYSTEM);
                // 判断模板类容是否为空
                if (sysNotificationTemplate != null) {
                    // 判断是否推送系统消息
                    if (sysNotificationChannelConfig.getIsSystem() == BooleanEnum.IS_TRUE) {
                        messageDealService.pushOrderAppealCreateOrCancelMessage(appeal, sysNotificationTemplate, appeal.getOrder());
                    }
                }
            }
        } else {
            log.warn("============系统消息推送，无{}配置，订单id：{}============", MonitorTriggerEvent.OTC_APPEAL_CANCEL.getCnName(), carrier.getRefId());
        }
    }
}