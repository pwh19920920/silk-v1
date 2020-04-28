package com.spark.bitrade.vendor.provider.support;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.transform.SmsEnity;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Slf4j
//@Service
public class KafkaSMSProvider implements SMSProviderProxy {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private SmsEnity createSmsEnity(String mobile, String content) {
        SmsEnity smsEnity = new SmsEnity();
        smsEnity.setToPhone(mobile);
        smsEnity.setValidDate(new Date());
        smsEnity.setContent(content);

        try {
            Class<?> clazz = Class.forName("org.springframework.web.context.request.RequestContextHolder");
            Object obj = clazz.getMethod("getRequestAttributes").invoke(null);
            HttpServletRequest request = (HttpServletRequest) obj.getClass().getMethod("getRequest").invoke(obj);
            HttpSession session = request.getSession();
            clazz = Class.forName("com.spark.bitrade.constant.SysConstant");
            String attribute = (String) clazz.getField("SESSION_MEMBER").get(null);
            Object authMember = session.getAttribute(attribute);
            String thirdMark = (String) authMember.getClass().getMethod("getPlatform").invoke(authMember);
            smsEnity.setThirdMark(thirdMark);
        } catch (Exception e) {
            log.error("获取来源标识异常: {}", e.getMessage());
        }
        return smsEnity;
    }

    @Override
    public String formatVerifyCode(String code) {
        return String.format("【SilkTrader】您的验证码为：%s，请按页面提示填写，切勿泄露于他人。", code);
    }

    @Override
    public MessageResult sendMessage(String mobile, String verifyCode) {
        return sendSingleMessage(mobile, verifyCode);
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String verifyCode) {
        String content = formatVerifyCode(verifyCode);
        return sendSingleMessage(mobile, content);
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) {
        sendKafkaMessage("sendSingleMessage", JSON.toJSONString(createSmsEnity(mobile, content)));
        return MessageResult.success();
    }

    @Override
    public MessageResult sendInternationalMessage(String content, String phone) {
        content = String.format("【SilkTrader】Your verification code is %s, Please fill in according the instructions. Do not reveal it to others.", content);
        sendKafkaMessage("sendSingleMessage", JSON.toJSONString(createSmsEnity(phone, content)));
        return MessageResult.success();
    }

    @Override
    public MessageResult sendComplexMessage(String content, String phone) {
        content = String.format("【SilkTrader】您的驗證碼為：%s，請按頁面提示填寫，切勿泄露於他人。", content);
        sendKafkaMessage("sendSingleMessage", JSON.toJSONString(createSmsEnity(phone, content)));
        return MessageResult.success();
    }

    @Override
    public MessageResult batchSend(String text, String mobile) {
        sendKafkaMessage("batchSend", JSON.toJSONString(createSmsEnity(mobile, text)));
        return MessageResult.success();
    }

    @Override
    public MessageResult sendVoiceCode(String mobile, String voiceCode) {
        sendKafkaMessage("sendVoiceCode", JSON.toJSONString(createSmsEnity(mobile, voiceCode)));
        return MessageResult.success();
    }

    @Async
    public void sendKafkaMessage(String key, String data) {
        kafkaTemplate.send(SmsEnity.MSG_SMS_HANDLER, key, data);
    }
}
