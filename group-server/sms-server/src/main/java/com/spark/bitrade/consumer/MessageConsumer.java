package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.transform.SmsEnity;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vendor.provider.SMSProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *  * 消费kafka中收到的数据
 *  * @author tansitao
 *  * @time 2018/12/17 17:04 
 *  
 */
@Slf4j
@Component
public class MessageConsumer {
    @Autowired
    @Qualifier("smsProvider")
    private SMSProvider smsProvider;
    //是否为模拟环境
    @Value("${sms.isMock:true}")
    private boolean isMock;
    private static final Map<String, String> smsSign = new HashMap<>();

    static {
        // 0：表示星客交易所会员;1：表示升腾会员;2：表示链人会员;3：表示星客CNYT理财会员;4：表示星客钱包会员
        smsSign.put("0", "SilkTrader");
        smsSign.put("1", "升腾钱包");
        smsSign.put("2", "链人钱包");
        smsSign.put("3", "CNYT理财");
        smsSign.put("4", "星客钱包");
        smsSign.put("55580235", "SilkPay");
    }

    @KafkaListener(topics = SmsEnity.MSG_SMS_HANDLER, group = "group-handle")
    public void handleMessage(ConsumerRecord<String, String> record) {
        getMessageConsumer().asyncHandleMessage(record);
    }

    public MessageConsumer getMessageConsumer(){
        return SpringContextUtil.getBean(MessageConsumer.class);
    }

    @Async
    public void asyncHandleMessage(ConsumerRecord<String, String> record) {
        try {
            log.info("topic={},key={},value={}", record.topic(), record.key(), record.value());
            SmsEnity smsEnity = JSON.parseObject(record.value(), SmsEnity.class);
            if (smsEnity != null && (null != smsEnity.getValidDate() && DateUtil.addMinToDate(smsEnity.getValidDate(), 10).compareTo(new Date()) >= 0)) {
                String signText = smsSign.getOrDefault(smsEnity.getThirdMark(), "SilkTrader");
                String content = smsEnity.getContent().replace("SilkTrader", signText);
                if (!content.matches("^【.+】.+")) {
                    content = String.format("【%s】", signText) + content;
                }
                smsEnity.setContent(content);
                if (isMock) {
                    log.info("当前为短信发送模拟效果，短信的内容：{}", smsEnity);
                } else {
                    //发送短信
                    MessageResult result;
                    switch (record.key()) {
                        case "sendSingleMessage":
                            result = smsProvider.sendSingleMessage(smsEnity.getToPhone(), smsEnity.getContent());
                            break;
                        case "batchSend":
                            result = smsProvider.batchSend(smsEnity.getContent(), smsEnity.getToPhone());
                            break;
                        case "sendVoiceCode":
                            result = smsProvider.sendVoiceCode(smsEnity.getToPhone(), smsEnity.getContent());
                            break;
                        default:
                            result = smsProvider.sendSingleMessage(smsEnity.getToPhone(), smsEnity.getContent());
                    }
                    log.info("发送短信返回结果：{}", result);
                }
            } else {
                log.info("kafka中的数据已过期，不需要发送短信：{}", smsEnity);
            }
        } catch (Exception e) {
            log.error("======短信发送失败，原因：{}=====", e);
        }
    }
}
