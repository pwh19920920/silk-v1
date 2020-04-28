package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.transform.SmsEnity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/***
 * 
 * @author yangch
 * @time 2018.05.26 17:08
 */

@RestController
public class HealthyController {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @RequestMapping("/sleep/{sleepTime}")
    public String sleep(@PathVariable Long sleepTime) throws InterruptedException {
        //超时测试接口
        int i =0/1;
        TimeUnit.SECONDS.sleep(sleepTime);
        return "SUCCESS";
    }

    /**
     * 测试kafka发送短信
     * @author tansitao
     * @time 2018/12/17 17:31 
     */
    @RequestMapping("send2")
    @ResponseBody
    public String sendRegEmailCheckCode2(String to) throws Exception {
        if(to == null) {
            to = "15723175459";
        }
        SmsEnity smsEnity = new SmsEnity();
        smsEnity.setAreaCode("86");
        smsEnity.setContent("【SilkTrader】您的验证码为：789456，请按页面提示填写，切勿泄露于他人。");
        smsEnity.setToPhone(to);
        smsEnity.setValidDate(new Date());
        kafkaTemplate.send(SmsEnity.MSG_SMS_HANDLER,
                to, JSON.toJSONString(smsEnity));
        return "ok";
    }


}
