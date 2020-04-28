package com.spark.bitrade.messager.controller;

import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.messager.service.INoticeService;
import com.spark.bitrade.messager.util.ResourceHelper;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * @author ww
 * @time 2019.09.10 09:53
 */


public class NoticeController {



    //要优化到业务Service 里面去
    @Autowired
    KafkaTemplate kafkaTemplate;


    // 本地配置文件读取

    ResourceHelper resourceHelper = new ResourceHelper();
    @Resource
    MessageSource messageSource;

    @Autowired
    INoticeService noticeService;




    //@ApiOperation("sendNoticeEntity")
    //@RequestMapping("sendNoticeEntity")

    //@ApiOperation(value = "发送消息体NoticeEntity" ,notes = "发送消息体NoticeEntity",httpMethod = "POST")


    //@RequestMapping("send")
    public MessageResult send(NoticeEntity noticeEntity){

        noticeService.sendToKafka(noticeEntity);
        return MessageResult.success();
    }

    String getResourceMessage(String s){
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(s, null, locale);
    }

}
