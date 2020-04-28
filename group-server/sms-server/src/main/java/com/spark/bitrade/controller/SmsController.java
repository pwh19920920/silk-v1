package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.transform.SmsEnity;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.ValidateUtil;
import com.spark.bitrade.vendor.provider.SMSProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/***
 * 
 * @author yangch
 * @time 2018.05.26 17:08
 */

@RestController
@Slf4j
public class SmsController {

    @Autowired
    @Qualifier("smsProvider")
    private SMSProvider smsProvider;

    //是否为模拟环境
    @Value("${sms.isMock:true}")
    private boolean isMock;

    /**
     * 发送短信
     * @param smsEnity 短信内容实体类
     * @return
     */
    @RequestMapping("/send/enity")
    public MessageResult sendSms(@RequestBody SmsEnity smsEnity){
        try {
            if(smsEnity != null){
                if ( null != smsEnity.getValidDate()
                        && smsEnity.getValidDate().compareTo(new Date()) >= 0){
                    log.warn("短信内容已过期。短信信息：{}", smsEnity);
                    return MessageResult.error("短信内容已过期");
                }

                if(ValidateUtil.isMobilePhone(smsEnity.getToPhone())) {
                    if(isMock){
                        log.info("当前为短信发送模拟效果，短信的内容：{}", smsEnity);
                        return MessageResult.success();
                    } else {
                        //发送短信
                        return smsProvider.sendSingleMessage(smsEnity.getToPhone(), smsEnity.getContent());
                    }
                } else {
                    log.warn("用户（{}）的手机号为空或格式错误", smsEnity.getToPhone());
                    return MessageResult.error("手机号为空或格式错误");
                }
            } else {
                log.warn("短信实体为null");
                return MessageResult.error("短信实体为null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("短信发送失败，原因：{}", e.getMessage());
        }

        return MessageResult.error("原因未知");
    }

    /**
     * 发送短信
     * @param jsonSmsEnity
     * @return
     */
    @RequestMapping("/send/json")
    public MessageResult sendSms(String jsonSmsEnity){
        try {
            if(StringUtils.isEmpty(jsonSmsEnity)){
                log.info("短信内容为空，发送内容请参考SmsEnity实体");
                return MessageResult.error("短信内容为空");
            }

            SmsEnity smsEnity = JSON.parseObject(jsonSmsEnity, SmsEnity.class);

            //发送短信
            sendSms(smsEnity);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("短信发送失败，原因：{}", e.getMessage());
        }

        return MessageResult.error("原因未知");
    }

    /**
     * 发送短信
     * @author tansitao
     * @time 2018/12/17 17:31 
     */
    @RequestMapping("send")
    public MessageResult sendSms(String phone, String content, String areaCode) throws Exception {
        SmsEnity smsEnity = new SmsEnity();
        smsEnity.setAreaCode(areaCode);
        smsEnity.setContent(content);
        smsEnity.setToPhone(phone);
        smsEnity.setValidDate(new Date());
        log.info("=============通过接口发送短信============{}", JSON.toJSONString(smsEnity));
        if (ValidateUtil.isMobilePhone(phone)) {
            if(isMock){
                log.info("当前为短信发送模拟效果，短信的内容：{}", smsEnity);
            } else {
                //发送短信
                smsProvider.sendSingleMessage(smsEnity.getToPhone(), smsEnity.getContent());
            }
        }else {
            log.warn("=========用户（{}）的手机号为空或格式错误===========", phone);
        }
        return MessageResult.success();
    }


}
