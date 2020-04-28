package com.spark.bitrade.service.business.impl;

import com.spark.bitrade.constant.InfoType;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.business.IBusinessApprovedEnventHandler;
import com.spark.bitrade.service.CommonSilkPlatInformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 商家认证审核事件通知处理实现类
 *
 * @author zhongxj
 * @time 2019.09.19
 */
@Service
@Slf4j
public class BusinessApprovedMessageHandlerImpl implements IBusinessApprovedEnventHandler {
    @Autowired
    private CommonSilkPlatInformationService commonSilkPlatInformationService;

    @Override
    public void handle(CollectCarrier carrier) {
        Long memberId = Long.valueOf(carrier.getRefId());
        log.info("================={}=================memberId:{}", carrier.getCollectType().getCnName(), memberId);
        String country = commonSilkPlatInformationService.getCountry(memberId);
        if (country != null && !"".equals(country)) {
            // 发送通知
            commonSilkPlatInformationService.sendCommonNotice(null, memberId, InfoType.MERCHANT_CERTIFICATION_PASSED, country, null, null, NoticeType.SYS_NOTICE_BUSINESS_VERFIY);
            // 发送邮件
            commonSilkPlatInformationService.sendCommonEmail(null, memberId, InfoType.MERCHANT_CERTIFICATION_PASSED, country, null);
        } else {
            log.info("================={}=================memberId:{}，未找到相关数据", carrier.getCollectType().getCnName(), memberId);
        }
    }
}
