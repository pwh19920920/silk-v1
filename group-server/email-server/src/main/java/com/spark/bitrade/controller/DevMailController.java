package com.spark.bitrade.controller;

import com.spark.bitrade.entity.transform.EmailEnity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.internet.MimeMessage;

/**
 * 邮件，新浪发送《临时使用，现已弃用》
 */
@RestController
@RequestMapping("/mail")
@Slf4j
public class DevMailController {
    @Autowired
    JavaMailSender mailSender;

    @ResponseBody
    @RequestMapping("/dev/send")
    public void sendEmail(@RequestBody EmailEnity emailEnity) {
        log.info("===========emailEnity:{}", emailEnity);
        try {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
            message.setFrom("2505690640@qq.com");
            message.setTo(emailEnity.getToEmail());
            message.setSubject(emailEnity.getSubject());
            message.setText(emailEnity.getHtmlConent(), true);
            this.mailSender.send(mimeMessage);
            log.info("success");
        } catch (Exception ex) {
            log.info("error");
            ex.printStackTrace();
        }
    }
}
