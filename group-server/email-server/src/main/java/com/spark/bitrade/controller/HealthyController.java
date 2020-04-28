package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.MultiMailConfiguration;
import com.spark.bitrade.entity.transform.EmailEnity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/***
 * 
 * @author yangch
 * @time 2018.05.26 17:08
 */

@RestController
public class HealthyController {

    @Autowired
    MultiMailConfiguration multiMailConfiguration;
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
      * 发送邮件测试
      * @author yangch
      * @time 2018.04.13 15:57
      */
    @RequestMapping("send1")
    @ResponseBody
    public String sendRegEmailCheckCode(String to) throws Exception {
        if(to == null) {
            to = "yangconghong@dingtalk.com";
        }

        multiMailConfiguration.sentEmailHtml(to, "test-title", "text-html---");
        return "ok";
    }

    /**
      * 发送邮件测试
      * @author yangch
      * @time 2018.04.13 15:57
      */
    @RequestMapping("send2")
    @ResponseBody
    public String sendRegEmailCheckCode2(String to) throws Exception {
        if(to == null) {
            to = "yangconghong@dingtalk.com";
        }

        EmailEnity emailEnity = new EmailEnity();
        emailEnity.setToEmail(to);
        emailEnity.setSubject("test-title2");
        emailEnity.setHtmlConent("text-html---22");
        System.out.println(JSON.toJSONString(emailEnity));

        kafkaTemplate.send(EmailEnity.MSG_EMAIL_HANDLER,
                to, JSON.toJSONString(emailEnity));
        //multiMailConfiguration.sentEmailHtml(to, "test-title", "text-html---");
        return "ok";
    }


}
