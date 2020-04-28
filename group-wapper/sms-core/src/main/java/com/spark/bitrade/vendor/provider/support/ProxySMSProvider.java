package com.spark.bitrade.vendor.provider.support;

import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vendor.provider.SMSProvider;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ProxySMSProvider implements SMSProviderProxy {

    @Autowired
    private SMSProvider smsProvider;

    public MessageResult sendSingleMessage(String mobile, String content) throws Exception {
        return smsProvider.sendSingleMessage(mobile, content);
    }

    public String formatVerifyPhone(String phone) {
        return smsProvider.formatVerifyPhone(phone);
    }

    public String formatVerifyLogin() {
        return smsProvider.formatVerifyLogin();
    }

    public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        return smsProvider.sendVerifyMessage(mobile, verifyCode);
    }

    public MessageResult sendMessage(String mobile, String content) throws Exception {
        return smsProvider.sendMessage(mobile, content);
    }

    public String formatVerifyCode(String code) {
        return smsProvider.formatVerifyCode(code);
    }

    public MessageResult sendInternationalMessage(String content, String phone) throws IOException, DocumentException {
        return smsProvider.sendInternationalMessage(content, phone);
    }

    public MessageResult sendComplexMessage(String content, String phone) throws IOException, DocumentException {
        return smsProvider.sendComplexMessage(content, phone);
    }

    public MessageResult batchSend(String text, String mobile) throws Exception {
        return smsProvider.batchSend(text, mobile);
    }

    public MessageResult sendVoiceCode(String mobile, String voiceCode) {
        return smsProvider.sendVoiceCode(mobile, voiceCode);
    }
}
