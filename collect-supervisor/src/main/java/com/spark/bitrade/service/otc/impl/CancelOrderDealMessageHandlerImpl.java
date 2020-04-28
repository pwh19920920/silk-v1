package com.spark.bitrade.service.otc.impl;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.constant.NotificationType;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.SysNotificationChannelConfig;
import com.spark.bitrade.entity.SysNotificationTemplate;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.otc.ICancelOrderEnventHandler;
import com.spark.bitrade.service.MessageDealService;
import com.spark.bitrade.service.OtcOrderService;
import com.spark.bitrade.service.impl.SysNotificationChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * C2C订单撤销事件通知处理实现类
 *
 * @authortansitao
 * @time2018/12/2410:32
 */
@Service
@Slf4j
public class CancelOrderDealMessageHandlerImpl implements ICancelOrderEnventHandler {
    @Autowired
    private SysNotificationChannelConfigService sysNotificationChannelConfigService;
    @Autowired
    private MessageDealService messageDealService;
    @Autowired
    private OtcOrderService otcOrderService;

    @Override
    public void handle(CollectCarrier carrier) {
        log.info("================={}=================orderSn:{}=================memberId:{}", carrier.getCollectType().getCnName(), carrier.getRefId(), carrier.getMemberId());
        List<SysNotificationChannelConfig> sncConfigList = sysNotificationChannelConfigService.findByType(MonitorTriggerEvent.OTC_CANCEL_ORDER.getOrdinal());
        if (sncConfigList != null && sncConfigList.size() > 0) {
            for (SysNotificationChannelConfig sysNotificationChannelConfig : sncConfigList) {
                Order order = otcOrderService.findOneByOrderId(carrier.getRefId());
                if (order != null) {
                    SysNotificationTemplate sysNotificationTemplate = null;
                    // 判断撤销订单类型
                    if (order.getIsManualCancel() == BooleanEnum.IS_TRUE) {
                        // 获取主动订单撤销系统消息内容模板
                        sysNotificationTemplate = messageDealService.getSystemMessage(carrier, MonitorTriggerEvent.OTC_MANUAL_CANCEL_ORDER, NotificationType.SYSTEM);
                    } else {
                        // 获取被动订单撤销系统消息内容模板
                        sysNotificationTemplate = messageDealService.getSystemMessage(carrier, MonitorTriggerEvent.OTC_AUTO_CANCEL_ORDER, NotificationType.SYSTEM);
                    }
                    // 判断模板类容是否为空
                    if (sysNotificationTemplate != null) {
                        // 判断是否推送系统消息
                        if (sysNotificationChannelConfig.getIsSystem() == BooleanEnum.IS_TRUE) {
                            messageDealService.pushCancelOrderMessage(order, sysNotificationTemplate);
                        }
                    }
                } else {
                    log.warn("============系统消息推送，{},订单不存在id{}============", carrier.getCollectType().getCnName(), carrier.getRefId());
                }
            }
        } else {
            log.warn("============系统消息推送，无{}配置，订单id：{}============", carrier.getCollectType().getCnName(), carrier.getRefId());
        }
    }
}