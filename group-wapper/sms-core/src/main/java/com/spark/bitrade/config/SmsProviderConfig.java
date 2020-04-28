package com.spark.bitrade.config;

import com.spark.bitrade.vendor.provider.SMSProvider;
import com.spark.bitrade.vendor.provider.support.ChuangRuiSMSProvider;
import com.spark.bitrade.vendor.provider.support.EmaySMSProvider;
import com.spark.bitrade.vendor.provider.support.HuaXinSMSProvider;
import com.spark.bitrade.vendor.provider.support.YunpianSMSProvider;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class SmsProviderConfig {

    @Value("${sms.gateway:}")
    private String gateway;
    @Value("${sms.username:}")
    private String username;
    @Value("${sms.password:}")
    private String password;
    @Value("${sms.sign:}")
    private String sign;
    @Value("${sms.internationalGateway:}")
    private String internationalGateway;
    @Value("${sms.internationalUsername:}")
    private String internationalUsername;
    @Value("${sms.internationalPassword:}")
    private String internationalPassword;
    @Value("${sms.newOrderContent:}")
    private String newOrderContent;
    @Value("${sms.payedContent:}")
    private String payedContent;
    @Value("${sms.releasedContent:}")
    private String releasedContent;

    @Value("${sms.newOrderContentEng:}")
    private String newOrderContentEng;
    @Value("${sms.payedContentEng:}")
    private String payedContentEng;
    @Value("${sms.releasedContentEng:}")
    private String releasedContentEng;

    @Value("${sms.newOrderContentCom:}")
    private String newOrderContentCom;
    @Value("${sms.payedContentCom:}")
    private String payedContentCom;
    @Value("${sms.releasedContentCom:}")
    private String releasedContentCom;

    @Bean
    public SMSProvider smsProvider(@Value("${sms.driver:}") String driverName) {
        if (StringUtils.isEmpty(driverName)) {
            return new ChuangRuiSMSProvider(gateway, username, password, sign);
        }
        if (driverName.equalsIgnoreCase(ChuangRuiSMSProvider.getName())) {
            return new ChuangRuiSMSProvider(gateway, username, password, sign);
        } else if (driverName.equalsIgnoreCase(EmaySMSProvider.getName())) {
            return new EmaySMSProvider(gateway, username, password);
        } else if (driverName.equalsIgnoreCase(HuaXinSMSProvider.getName())) {
            return new HuaXinSMSProvider(gateway, username, password, internationalGateway, internationalUsername, internationalPassword, sign);
        } else if (driverName.equalsIgnoreCase(YunpianSMSProvider.getName())) {
            return new YunpianSMSProvider(gateway, username);
        } else {
            return null;
        }
    }
}
