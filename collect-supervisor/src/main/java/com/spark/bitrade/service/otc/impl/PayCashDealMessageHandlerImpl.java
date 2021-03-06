package com.spark.bitrade.service.otc.impl;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.SysNotificationChannelConfig;
import com.spark.bitrade.entity.SysNotificationTemplate;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.otc.IPayCashEnventHandler;
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
 * C2C支付订单事件通知处理实现类
 *
 * @authortansitao
 * @time2018/12/209:17
 */
@Service
@Slf4j
public class PayCashDealMessageHandlerImpl implements IPayCashEnventHandler {
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
            List<SysNotificationChannelConfig> sncConfigList = sysNotificationChannelConfigService.findByType(MonitorTriggerEvent.OTC_PAY_CASH.getOrdinal());
            if (sncConfigList != null && sncConfigList.size() > 0) {
                for (SysNotificationChannelConfig sysNotificationChannelConfig : sncConfigList) {
                    // 获取支付订单系统消息内容模板
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

            // 推送通知、邮件 1SELL-出售，customer_id:买方，member_id：卖方，反之。
            Long businessMemberId = order.getMemberId();
            Long customerMemberId = order.getCustomerId();
            InfoType infoType = InfoType.OTC_PAY_CASH;
            String country = "";
            if (order.getAdvertiseType() == AdvertiseType.SELL) {
                country = commonSilkPlatInformationService.getCountry(businessMemberId);
            } else {
                country = commonSilkPlatInformationService.getCountry(customerMemberId);
            }
            Map<String, Object> map = commonSilkPlatInformationService.getCreateOrderModel(order, customerMemberId, null);
            if (country != null && !"".equals(country)) {
                // 被交易方
                if(order.getAdvertiseType() == AdvertiseType.SELL){
                    commonSilkPlatInformationService.sendCommonNotice(SysConstant.SELL, businessMemberId, infoType, country, map, order.getOrderSn(), NoticeType.SYS_NOTICE_OTC_ORDER);
                }else {
                    commonSilkPlatInformationService.sendCommonNotice(SysConstant.SELL, customerMemberId, infoType, country, map, order.getOrderSn(), NoticeType.SYS_NOTICE_OTC_ORDER);
                }

                commonSilkPlatInformationService.sendCommonEmail(SysConstant.SELL, businessMemberId, infoType, country, map);
            }
            // 发送短信
            if (order.getAdvertiseType() == AdvertiseType.BUY) {
                map = commonSilkPlatInformationService.getCreateOrderModel(order, businessMemberId, null);
                commonSilkPlatInformationService.sendCommonSms(SysConstant.SELL, customerMemberId, infoType, map);
            } else {
                commonSilkPlatInformationService.sendCommonSms(SysConstant.SELL, businessMemberId, infoType, map);
            }

        } else {
            log.warn("============系统消息推送，{},订单不存在id{}============", carrier.getCollectType().getCnName(), carrier.getRefId());
        }
    }
}