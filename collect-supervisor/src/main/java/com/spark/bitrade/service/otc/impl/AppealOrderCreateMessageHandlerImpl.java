package com.spark.bitrade.service.otc.impl;

import com.spark.bitrade.constant.AppealStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.constant.NotificationType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.otc.IAppealOrderEnventHandler;
import com.spark.bitrade.service.*;
import com.spark.bitrade.service.impl.SysNotificationChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
*C2C订单申诉创建事件通知处理实现类
*@authortansitao
*@time2018/12/2410:32
*/
@Service
@Slf4j
public class AppealOrderCreateMessageHandlerImpl implements IAppealOrderEnventHandler {
    @Autowired
    private SysNotificationChannelConfigService sysNotificationChannelConfigService;
    @Autowired
    private MessageDealService messageDealService;
    @Autowired
    private OtcOrderService otcOrderService;
    @Autowired
    private AppealService appealService;

    @Override
    public void handle(CollectCarrier carrier) {
        log.info("================={}=================orderSn:{}=================memberId:{}", carrier.getCollectType().getCnName(), carrier.getRefId(), carrier.getMemberId());
        Order order = otcOrderService.findOneByOrderId(carrier.getRefId());
        Appeal appeal = null;
        if(order != null){
            appeal = appealService.findNewByorderId(String.valueOf(order.getId()));
            if(appeal == null || appeal.getStatus() != AppealStatus.NOT_PROCESSED){
                log.info("==============申诉记录不存在，或者不是创建申诉事件===================orderSn:{}====memberId:{}", carrier.getRefId(), carrier.getMemberId());
                return;
            }
        }else{
            log.info("============系统消息推送，{},订单不存在id{}============", MonitorTriggerEvent.OTC_APPEAL_SUBMIT.getCnName(), carrier.getRefId());
            return;
        }

        List<SysNotificationChannelConfig> sncConfigList = sysNotificationChannelConfigService.findByType(MonitorTriggerEvent.OTC_APPEAL_SUBMIT.getOrdinal());
        if(sncConfigList != null && sncConfigList.size() > 0){
            for (SysNotificationChannelConfig sysNotificationChannelConfig:sncConfigList) {
                // 获取创建订单申诉系统消息内容模板
                SysNotificationTemplate sysNotificationTemplate = messageDealService.getSystemMessage(carrier, sysNotificationChannelConfig.getType(), NotificationType.SYSTEM);
                // 判断模板类容是否为空
                if(sysNotificationTemplate != null){
                    // 判断是否推送系统消息
                    if(sysNotificationChannelConfig.getIsSystem() == BooleanEnum.IS_TRUE){
                        messageDealService.pushOrderAppealCreateOrCancelMessage(appeal, sysNotificationTemplate, order);
                    }
                }
            }
        }else{
            log.warn("============系统消息推送，无{}配置，订单id：{}============",carrier.getCollectType().getCnName(), carrier.getRefId());
        }
    }
}