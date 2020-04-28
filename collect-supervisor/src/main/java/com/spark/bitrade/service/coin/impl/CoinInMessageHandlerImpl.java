package com.spark.bitrade.service.coin.impl;

import com.spark.bitrade.constant.InfoType;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.SilkPlatInformation;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.coin.ICoinInEnventHandler;
import com.spark.bitrade.service.CommonSilkPlatInformationService;
import com.spark.bitrade.service.OtcMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 充值到账事件通知处理实现类
 *
 * @author zhongxj
 * @time 2019.09.20
 */
@Service
@Slf4j
public class CoinInMessageHandlerImpl implements ICoinInEnventHandler {
    @Autowired
    private CommonSilkPlatInformationService commonSilkPlatInformationService;
    @Autowired
    private OtcMsgService otcMsgService;

    @Override
    public void handle(CollectCarrier carrier) {
        String txHash = carrier.getRefId();
        Long memberId = Long.valueOf(carrier.getMemberId());
        log.info("================={}=================memberId:{}=================txHash:{}", carrier.getCollectType().getCnName(), memberId, txHash);
        String country = commonSilkPlatInformationService.getCountry(memberId);
        if (country == null) {
            return;
        }
        Map map = commonSilkPlatInformationService.getCoinInModel(txHash);
        SilkPlatInformation silkPlatInformation = otcMsgService.getSilkPlatInformationByEvent(SysConstant.NO_LIMITATION, InfoType.COIN_IN.getOrdinal());
        if (!StringUtils.isEmpty(silkPlatInformation)) {
            if (silkPlatInformation.getUseInstation() == 1) {
                // 发送通知 系统通知开启
                commonSilkPlatInformationService.sendSaveAndCoinNotice(SysConstant.NO_LIMITATION, memberId, InfoType.COIN_IN, country, map, txHash, NoticeType.SYS_NOTICE_MAIL);
            } else {
                // 发送通知 系统通知关闭
                commonSilkPlatInformationService.sendCommonNotice(SysConstant.NO_LIMITATION, memberId, InfoType.COIN_IN, country, map, null, NoticeType.SYS_NOTICE_COIN_IN);
            }
        } else {
            log.info("================={}=================memberId:{}=================txHash:{}，模板内容为空", carrier.getCollectType().getCnName(), memberId, txHash);
        }
        // 发送邮件
        commonSilkPlatInformationService.sendCommonEmail(SysConstant.NO_LIMITATION, memberId, InfoType.COIN_IN, country, map);
    }
}
