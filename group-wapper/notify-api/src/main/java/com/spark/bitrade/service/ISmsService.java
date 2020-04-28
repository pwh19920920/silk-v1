package com.spark.bitrade.service;

import com.spark.bitrade.entity.transform.EmailEnity;
import com.spark.bitrade.entity.transform.SmsEnity;
import com.spark.bitrade.util.MessageResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/****
 * 提供短信接入服务
 * @author yangch
 * @time 2018.12.18 11:07
 */
@FeignClient("sms-server")
public interface ISmsService {

    /**
     * 发送短信
     * @param smsEnity SmsEnity实体类
     */
    @RequestMapping("/send/enity")
    MessageResult sendSms(@RequestParam(value = "smsEnity") SmsEnity smsEnity);

    /**
     * 发送短信
     * @param jsonSmsEnity SmsEnity实体类的json序列化
     */
    @RequestMapping("/send/json")
    MessageResult sendSms(@RequestParam(value = "jsonSmsEnity") String jsonSmsEnity);

    /**
     * 发送短信
     * @param phone 接收手机号
     * @param content 短信内容
     * @param areaCode 手机号国家标识
     */
    @RequestMapping("/send")
    MessageResult sendSms(@RequestParam(value = "phone") String phone,
                          @RequestParam(value = "content") String content,
                          @RequestParam(value = "areaCode", required = false) String areaCode);
}
