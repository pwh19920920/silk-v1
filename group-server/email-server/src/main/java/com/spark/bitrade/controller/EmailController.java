package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.MultiMailConfiguration;
import com.spark.bitrade.entity.transform.EmailEnity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/***
  * 
  * @author yangch
  * @time 2018.05.26 17:08
  */

@Slf4j
@RestController
public class EmailController {

    @Autowired
    MultiMailConfiguration multiMailConfiguration;

    /***
      * 发送邮件
      * @author yangch
      * @time 2018.12.18 10:21 
     * @param emailEnity
     */
    @RequestMapping("/send/enity")
    public void sendEmail(@RequestBody EmailEnity emailEnity) throws Exception {
        if (null != emailEnity) {
            if (null != emailEnity.getValidDate()
                    && emailEnity.getValidDate().compareTo(new Date()) >= 0) {
                log.warn("邮件实体为null或者邮件内容已失效。邮件信息：{}", emailEnity);
                return;
            }

            //发送邮件
            multiMailConfiguration.sentEmailHtml(emailEnity.getToEmail()
                    , emailEnity.getSubject(), emailEnity.getHtmlConent());
        }
    }

    @RequestMapping("/send/json")
    public void sendEmail(String jsonEmailEnity) {
        try {
            if (StringUtils.isEmpty(jsonEmailEnity)) {
                log.info("邮件的内容为空，发送内容请参考EmailEnity实体");
                return;
            }

            EmailEnity emailEnity = JSON.parseObject(jsonEmailEnity, EmailEnity.class);
            sendEmail(emailEnity);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败，原因：{}", e.getMessage());
        }
    }

    /**
     * 发送邮件
     *
     * @param toEmail    接收地址
     * @param subject    邮件主题
     * @param htmlConent 邮件内容，可以包含html标签
     */
    @RequestMapping("/send")
    public void sendEmail(String toEmail, String subject, String htmlConent) {
        log.info("发送邮件信息：toEmail={},subject={}, htmlConent={}", toEmail, subject, htmlConent);
        try {
            if (StringUtils.hasText(toEmail)
                    && StringUtils.hasText(subject)
                    && StringUtils.hasText(htmlConent)) {
                //发送邮件
                multiMailConfiguration.sentEmailHtml(toEmail, subject, htmlConent);
            } else {
                log.warn("不满足邮件发送要求");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败，原因：{}", e.getMessage());
        }
    }
}