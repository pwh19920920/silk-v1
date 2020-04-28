package com.spark.bitrade.service;

import com.spark.bitrade.entity.transform.EmailEnity;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/****
  * 提供邮件接入服务
  * @author yangch
  * @time 2018.12.18 11:07
  */
@FeignClient("email-server")
public interface IEmailService {

    /**
     * 发送邮件
     *
     * @param emailEnity EmailEnity实体类
     */
    @PostMapping("/email/send/enity")
    void sendEmailPro(@RequestBody EmailEnity emailEnity);

    /**
     * 发送邮件
     *
     * @param emailEnity EmailEnity实体类
     */
    @PostMapping("/email/mail/dev/send")
    void sendEmailDev(@RequestBody EmailEnity emailEnity);

    /**
     * 发送邮件
     *
     * @param jsonEmailEnity EmailEnity实体类的json序列化
     */
    @RequestMapping("/email/send/json")
    void sendEmail(@RequestParam(value = "jsonEmailEnity") String jsonEmailEnity);

    /**
     * 发送邮件
     *
     * @param toEmail    接收地址
     * @param subject    邮件主题
     * @param htmlConent 邮件内容，可以包含html标签
     */
    @RequestMapping("/email/send")
    void sendEmail(@RequestParam(value = "toEmail") String toEmail,
                   @RequestParam(value = "subject") String subject,
                   @RequestParam(value = "htmlConent") String htmlConent);
}

