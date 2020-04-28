package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.constant.InfoType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 未成功消费的，再次kafka写入
 *
 * @author zhongxj
 * @time 2019.09.27
 */
@Service
@Slf4j
public class KafkaDelayService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private PlatInstationService platInstationService;

    /**
     * getService
     *
     * @return
     */
    public KafkaDelayService getService() {
        return SpringContextUtil.getBean(KafkaDelayService.class);
    }

    /**
     * 发送kafka消息，C2C订单申诉胜诉
     *
     * @param refId 申诉id
     * @return
     */
    public void sendOtcAppealOrderComplete(String refId) {
        log.info("C2C订单申诉，审核完成，kafka重新写入，refId={}", refId);
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_APPEAL_ORDER_COMPLETE);
        carrier.setRefId(refId);
        String msg = JSON.toJSONString(carrier);
        kafkaTemplate.send("msg-collectcarrier", "OTC", msg);
    }

    /**
     * 运营手动编辑站内信，发送失败
     *
     * @param refId    站内信ID
     * @param infoType 事件类型
     */
    public void sendSaveNotice(String refId, Long memberId, InfoType infoType) {
        log.info("运营手动编辑站内信，发送失败，refId={},memberId={},infoType={}", refId, memberId, infoType);
        CollectCarrier carrier = new CollectCarrier();
        carrier.setRefId(refId);
        if (infoType.getOrdinal() == InfoType.MANUAL_INSTATION.getOrdinal()) {
            log.info("运营手动编辑站内信，状态改为发送失败，refId={},memberId={},infoType={}", refId, memberId, infoType);
            // 状态改为发送失败
            platInstationService.updatePlatInstation(Long.valueOf(refId));
        }
    }
}
