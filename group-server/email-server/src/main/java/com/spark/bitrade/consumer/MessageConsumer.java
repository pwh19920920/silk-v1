package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.MultiMailConfiguration;
import com.spark.bitrade.entity.transform.EmailEnity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/***
 * 
 * @author yangch
 * @time 2018.10.08 11:54
 */

@Slf4j
@Component
public class MessageConsumer {
    @Autowired
    MultiMailConfiguration multiMailConfiguration;

    @KafkaListener(topics = EmailEnity.MSG_EMAIL_HANDLER,group = "group-handle")
    public void handleMessage(ConsumerRecord<String,String> record){
        getMessageConsumer().asyncHandleMessage(record);
    }

    public MessageConsumer getMessageConsumer(){
        return SpringContextUtil.getBean(MessageConsumer.class);
    }

    @Async
    public void asyncHandleMessage(ConsumerRecord<String,String> record){
        try {
            log.info("topic={},key={},value={}", record.topic(), record.key(), record.value());

            EmailEnity emailEnity = JSON.parseObject(record.value(), EmailEnity.class);
            if(emailEnity == null
                    || (null != emailEnity.getValidDate()
                    && emailEnity.getValidDate().compareTo(new Date()) >= 0)){

                log.warn("邮件实体为null或者邮件内容已失效。邮件信息：{}", emailEnity);
                return ;
            }

            //发送邮件
            multiMailConfiguration.sentEmailHtml(emailEnity.getToEmail()
                    , emailEnity.getSubject(), emailEnity.getHtmlConent());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败，原因：{}", e.getMessage());
        }
    }
}
