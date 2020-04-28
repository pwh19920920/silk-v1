package com.spark.bitrade.service.customizing;

import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.customizing.ICustomizingEnventHandler;
import com.spark.bitrade.service.CommonSilkPlatInformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
  * 运营手动编辑站内信
  * @author zhongxj
  * @time 2019.09.29
  */
@Service
@Slf4j
public class CustomizingMessageHandlerImpl implements ICustomizingEnventHandler {
    @Autowired
    private CommonSilkPlatInformationService commonSilkPlatInformationService;

    @Override
    public void handle(CollectCarrier carrier) {
        String refId = carrier.getRefId();
        log.info("================={}=================refId:{}", carrier.getCollectType().getCnName(), refId);
        // 发送通知
        commonSilkPlatInformationService.sendCustomizingNotice(refId, NoticeType.SYS_NOTICE_MAIL);
    }
}
