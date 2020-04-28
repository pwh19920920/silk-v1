package com.spark.bitrade.service.otc.impl;

import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.InfoType;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.otc.IExpireRemindOrderEnventHandler;
import com.spark.bitrade.service.CommonSilkPlatInformationService;
import com.spark.bitrade.service.OtcOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 *  * C2C订单即将过期事件通知处理实现类
 *  * @author zhongxj
 *  * @time 2019.09.20
 *  
 */
@Service
@Slf4j
public class ExpireRemindOrderMessageHandlerImpl implements IExpireRemindOrderEnventHandler {

    @Autowired
    private CommonSilkPlatInformationService commonSilkPlatInformationService;
    @Autowired
    private OtcOrderService otcOrderService;

    @Override
    public void handle(CollectCarrier carrier) {
        String triggerEventName = carrier.getCollectType().getCnName();
        String orderSn = carrier.getRefId();
        log.info("================={}=================orderSn:{},begin", triggerEventName, orderSn);
        Order order = otcOrderService.findOneByOrderId(carrier.getRefId());
        if (order != null) {
            // 推送通知、邮件 1SELL-出售，customer_id:买方，member_id：卖方，反之。
            Long businessMemberId = order.getMemberId();
            Long customerMemberId = order.getCustomerId();
            Long buyId = customerMemberId;
            Long sellId = businessMemberId;
            InfoType infoType = InfoType.OTC_EXPIRE_REMIND_ORDER;
            String customerCountry = "";
            String businessCountry = "";
            if (order.getAdvertiseType() == AdvertiseType.SELL) {
                customerCountry = commonSilkPlatInformationService.getCountry(customerMemberId);
                businessCountry = commonSilkPlatInformationService.getCountry(businessMemberId);
            } else {
                customerCountry = commonSilkPlatInformationService.getCountry(businessMemberId);
                businessCountry = commonSilkPlatInformationService.getCountry(customerMemberId);
                sellId = customerMemberId;
                buyId = businessMemberId;
            }
            if (customerCountry != null && !"".equals(customerCountry)) {
                Map<String, Object> map = commonSilkPlatInformationService.getExpireRemindOrderModel(order, sellId);
                // 交易方
                commonSilkPlatInformationService.sendCommonNotice(SysConstant.BUY, buyId, infoType, customerCountry, map, orderSn, NoticeType.SYS_NOTICE_OTC_ORDER);
                commonSilkPlatInformationService.sendCommonEmail(SysConstant.BUY, buyId, infoType, customerCountry, map);
            }
            if (businessCountry != null && !"".equals(businessCountry)) {
                Map<String, Object> map = commonSilkPlatInformationService.getExpireRemindOrderModel(order, buyId);
                // 被交易方
                commonSilkPlatInformationService.sendCommonNotice(SysConstant.SELL, sellId, infoType, businessCountry, map, orderSn, NoticeType.SYS_NOTICE_OTC_ORDER);
                commonSilkPlatInformationService.sendCommonEmail(SysConstant.SELL, sellId, infoType, businessCountry, map);
            }
            log.info("================={}=================orderSn:{},normal end", triggerEventName, orderSn);
        } else {
            log.info("================={}=================orderSn:{},order is null end", triggerEventName, orderSn);
        }
    }
}
