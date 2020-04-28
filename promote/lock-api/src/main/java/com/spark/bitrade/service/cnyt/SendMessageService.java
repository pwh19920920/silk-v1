package com.spark.bitrade.service.cnyt;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.LockConstant;
import com.spark.bitrade.entity.LockMarketRewardDetail;
import com.spark.bitrade.mq.CnytMarketRewardMessage;
import com.spark.bitrade.mq.CnytMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/***
 * 发送消息
 * @author yangch
 * @time 2018.12.04 9:16
 */

@Slf4j
@Service
public class SendMessageService {
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    private InviterRewardService inviterRewardService;

    /**
     * 发送kafka消息
     * @param message
     */
    public void sendCnytMessage(CnytMarketRewardMessage message){
        kafkaTemplate.send(LockConstant.KAFKA_TX_CNYT_MESSAGE_HANDLER,
                String.valueOf(message.getType()),
                JSON.toJSONString(message));
    }

    /**
     * 推送cnyt返佣处理的消息
     * @param inviteeRewardDetail 被推荐者的奖励明细记录
     * @param inviterRewardDetail 推荐者的奖励明细记录
     * @param type 消息类型（处理直推奖、处理级差奖、处理培养奖）
     * @param pervMessage 前一条消息，可以为null
     */
    public void sendCnytMessage(LockMarketRewardDetail inviteeRewardDetail,
                                LockMarketRewardDetail inviterRewardDetail,
                                CnytMessageType type, CnytMarketRewardMessage pervMessage){
        if(StringUtils.isEmpty(inviteeRewardDetail)){
            log.info("被推荐人奖励明细记录为null");
            return;
        }

        if(StringUtils.isEmpty(inviterRewardDetail)){
            log.info("推荐人奖励明细记录为null");
            return;
        }
        if(type == CnytMessageType.PERFORMANCE
                ||  type == CnytMessageType.LEVEL
                || type == CnytMessageType.REALTIME_REWARD) {
            log.warn("不支持PERFORMANCE（更新业绩）、LEVEL（更新等级）和REALTIME_REWARD（实时返佣）消息");
            return;
        }

        CnytMarketRewardMessage pushMessage =
                inviterRewardService.getCnytMarketRewardMessage(inviteeRewardDetail,
                inviterRewardDetail, type, pervMessage);
        sendCnytMessage(pushMessage);
    }

    /**
     * 发送更新业绩、等级的消息
     * @param inviteeRewardDetail 被推荐者的奖励明细记录
     * @param type  消息类型(PERFORMANCE、LEVEL,REALTIME_REWARD)
     */
    public void sendCnytUpdateMessage(LockMarketRewardDetail inviteeRewardDetail,
                                CnytMessageType type){
        if(StringUtils.isEmpty(inviteeRewardDetail)){
            log.info("被推荐人奖励明细记录为null");
            return;
        }

        if(type != CnytMessageType.PERFORMANCE
                &&  type != CnytMessageType.LEVEL
                && type != CnytMessageType.REALTIME_REWARD) {
            log.warn("仅支持PERFORMANCE（更新业绩）、LEVEL（更新等级）和REALTIME_REWARD（实时返佣）消息");
            return;
        }

        CnytMarketRewardMessage pushMessage =
                inviterRewardService.getCnytMarketRewardMessage(inviteeRewardDetail, type);
        sendCnytMessage(pushMessage);
    }

}
