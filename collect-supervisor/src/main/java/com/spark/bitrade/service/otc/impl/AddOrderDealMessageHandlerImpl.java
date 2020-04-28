package com.spark.bitrade.service.otc.impl;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.SysNotificationChannelConfig;
import com.spark.bitrade.entity.SysNotificationTemplate;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.otc.IAddOrderEnventHandler;
import com.spark.bitrade.service.CommonSilkPlatInformationService;
import com.spark.bitrade.service.MessageDealService;
import com.spark.bitrade.service.OtcOrderService;
import com.spark.bitrade.service.impl.SysNotificationChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 创建C2C订单
 *
 * @author tansitao
 * @time 2018/12/20 9:17 
 */
@Service
@Slf4j
public class AddOrderDealMessageHandlerImpl implements IAddOrderEnventHandler {
    @Autowired
    private SysNotificationChannelConfigService sysNotificationChannelConfigService;
    @Autowired
    private MessageDealService messageDealService;
    @Autowired
    private OtcOrderService otcOrderService;
    @Autowired
    private CommonSilkPlatInformationService commonSilkPlatInformationService;

    @Override
    public void handle(CollectCarrier carrier) {
        log.info("================={}=================orderSn:{}=================memberId:{}", carrier.getCollectType().getCnName(), carrier.getRefId(), carrier.getMemberId());
        Order order = otcOrderService.findOneByOrderId(carrier.getRefId());
        if (order != null) {
            // 聊天框系统消息推送、消息组件
            List<SysNotificationChannelConfig> sncConfigList = sysNotificationChannelConfigService.findByType(MonitorTriggerEvent.OTC_ADD_ORDER.getOrdinal());
            if (sncConfigList != null && sncConfigList.size() > 0) {
                for (SysNotificationChannelConfig sysNotificationChannelConfig : sncConfigList) {
                    // 获取创建订单系统消息内容模板
                    SysNotificationTemplate sysNotificationTemplate = messageDealService.getSystemMessage(carrier, sysNotificationChannelConfig.getType(), NotificationType.SYSTEM);
                    // 判断模板类容是否为空
                    if (sysNotificationTemplate != null) {
                        // 判断是否推送系统消息
                        if (sysNotificationChannelConfig.getIsSystem() == BooleanEnum.IS_TRUE) {
                            messageDealService.pushCreateOrPayOrderMessage(order, sysNotificationTemplate);
                        }
                    }
                }
            } else {
                log.warn("============无{}配置，订单id{}============", carrier.getCollectType(), carrier.getRefId());
            }

            // 邮件推送 1SELL-出售，customer_id:买方，member_id：卖方，反之。
            Long businessMemberId = order.getMemberId();
            Long customerMemberId = order.getCustomerId();
            InfoType infoType = InfoType.OTC_ADD_ORDER_TO_ADVERTISE;
            String advertiseType = "";
            String country = "";
            if (order.getAdvertiseType() == AdvertiseType.SELL) {
                country = commonSilkPlatInformationService.getCountry(businessMemberId);
                advertiseType = country.equals(SysConstant.ZH_LANGUAGE) ? "出售" : "sell";
            } else {
                country = commonSilkPlatInformationService.getCountry(customerMemberId);
                advertiseType = country.equals(SysConstant.ZH_LANGUAGE) ? "购买" : "buy";
            }
            Map<String, Object> map = commonSilkPlatInformationService.getCreateOrderModel(order, customerMemberId, advertiseType);
            // 被交易方
            if (country != null && !"".equals(country)) {
                commonSilkPlatInformationService.sendCommonNotice(SysConstant.NO_LIMITATION, businessMemberId, infoType, country, map, order.getOrderSn(), NoticeType.SYS_NOTICE_OTC_ORDER);
                commonSilkPlatInformationService.sendCommonEmail(SysConstant.NO_LIMITATION, businessMemberId, infoType, country, map);
            }
            // 发送短信
            commonSilkPlatInformationService.sendCommonSms(SysConstant.NO_LIMITATION, businessMemberId, infoType, map);
        } else {
            log.warn("============系统消息推送，{},订单不存在id{}============", carrier.getCollectType().getCnName(), carrier.getRefId());
        }
    }
}
