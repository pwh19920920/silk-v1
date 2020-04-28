package com.spark.bitrade.vendor.provider;


import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.Date;

public interface SMSProvider {
    /**
     * 发送单条短信
     *
     * @param mobile  手机号
     * @param content 短信内容
     * @return
     * @throws Exception
     */
    MessageResult sendSingleMessage(String mobile, String content) throws Exception;

    /*
    * 更换手机号警示短信格式
    * @author Zhang Yanjun
    * @time 2018.07.12 9:15
     * @param phone
    * @return  * @param phone
    */
    default String formatVerifyPhone(String phone){
        //158****5825
        String phoneNumber=phone.substring(0,3)+"****"+phone.substring(7,phone.length());
        return String.format("【SilkTrader】您绑定的手机号在"+ DateUtil.getNewDate()+" 已被修改为%s，若非本人操作，请在24小时内联系客服申诉。",phoneNumber);
    }


    /*
    * 登录成功后警示短信格式
    * @author Zhang Yanjun
    * @time 2018.07.12 14:07
     * @param
    * @return  * @param
    */
    default String formatVerifyLogin(){
        String date=DateUtil.getNewDate();
        return String.format("【SilkTrader】您在%s 登录SilkTrader，若非本人操作，请及时修改登录密码",date);
    }

    /**
     * 发送验证码短信
     *
     * @param mobile     手机号
     * @param verifyCode 验证码
     * @return
     * @throws Exception
     */
    default MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        return sendSingleMessage(mobile, formatVerifyCode(verifyCode));
    }

    /**
     * 发送短信
     * @param mobile
     * @param content
     * @return
     * @throws Exception
     */
    default MessageResult sendMessage(String mobile, String content) throws Exception {
        return sendSingleMessage(mobile, content);
    }

    /**
     * 获取验证码信息格式
     *
     * @param code
     * @return
     */
    default String formatVerifyCode(String code) {
        return String.format("您的验证码为%s，十分钟内有效，如非本人操作，请忽略", code);
    }

    /**
     * 发送国际短信
     *
     * @param content
     * @param phone
     * @return
     */
    default MessageResult sendInternationalMessage(String content, String phone) throws IOException, DocumentException {
        return null;
    }

    /**
     * 发送繁体短信
     * @author tansitao
     * @time 2018/9/12 15:45 
     */
    default MessageResult sendComplexMessage(String content, String phone) throws IOException, DocumentException {
        return null;
    }

    MessageResult batchSend( String text, String  mobile)  throws Exception;

    /**
     * 发送语音验证码
     * @author fumy
     * @time 2018.08.09 10:32
     * @param mobile
     * @param voiceCode
     * @return true
     */
    MessageResult sendVoiceCode(String mobile,String voiceCode);
}
