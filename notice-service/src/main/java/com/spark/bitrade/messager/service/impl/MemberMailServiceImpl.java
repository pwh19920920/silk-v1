package com.spark.bitrade.messager.service.impl;

import com.alibaba.fastjson.JSON;
//import com.spark.bitrade.messager.dao.MemberMailMapper;
import com.spark.bitrade.constant.messager.NoticeTag;
import com.spark.bitrade.messager.model.MemberMailContent;
import com.spark.bitrade.messager.model.MemberMailEntity;
import com.spark.bitrade.messager.dao.MemberMailMapper;
import com.spark.bitrade.messager.service.IMemberMailContentService;
import com.spark.bitrade.messager.service.IMemberMailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/*
 * @author ww
 * @time 2019.09.17 09:22*/


@Slf4j
@Component
public class MemberMailServiceImpl implements IMemberMailService {


    @Autowired
    KafkaTemplate kafkaTemplate;


    @Autowired
    MemberMailMapper memberMailMapper;

    @Autowired
    IMemberMailContentService memberMailContentService;


    @Override
    public MemberMailEntity getMailById(long id) {

        MemberMailEntity memberMail = memberMailMapper.getMailById(id);

        return memberMail;
    }

    @Override
    public List<MemberMailEntity> getMailsByMemberId(long memberId) {
        return null;
    }

    @Override
    public List<MemberMailEntity> getMailsByMemberId(long memberId, int size,int status, long start) {
        //限制大量查询
        if(size>50) size = 50;



        return  memberMailMapper.getMails(memberId,status,size,start);
        //if(status>-1) return memberMailMapper.getMailsByStatusAndMemberId(memberId,status,size,start);
        //else return memberMailMapper.getMailsByMemberId(memberId,size,start);

    }




    @Override
    public int insert(MemberMailEntity memberMailEntity) {



        memberMailEntity.setCreateTime(System.currentTimeMillis());

        //public MemberMailContent getContent() {
         //   if(null == content) content = memberMailContentService.getMailContentById(contentId);
          //  return content;
        //}


        //如果内容ID为空 则先保存 content
        if ((null == memberMailEntity.getContentId() || 0== memberMailEntity.getContentId())&& null != memberMailEntity.getContent()){

            MemberMailContent memberMailContent = new MemberMailContent();
            memberMailContent.setContent(memberMailEntity.getContent());
            if( memberMailContentService.insert(memberMailContent)>0)
            memberMailEntity.setContentId(memberMailContent.getId());
        }

        return memberMailMapper.insert(memberMailEntity);
    }

    @Override
    public MemberMailEntity getMailByIdWithUserId(Long id, long memberId) {
        return memberMailMapper.getMailByIdWithUserId(id,memberId);
    }

    @Override
    public MemberMailEntity getMailWithContentByIdWithUserId(Long id, long memberId) {

        MemberMailEntity memberMail = getMailByIdWithUserId(id,memberId);
        MemberMailContent memberMailContent = memberMailContentService.getMailContentById(memberMail.getContentId());
        memberMail.setContent(memberMailContent.getContent());

        return memberMail;
    }

    @Override
    public MemberMailEntity getMailWithContentById(Long id) {

        MemberMailEntity memberMail = getMailById(id);
        if(null != memberMail.getContentId()){
            MemberMailContent memberMailContent = memberMailContentService.getMailContentById(memberMail.getContentId());
            memberMail.setContent(memberMailContent.getContent());
        }
        return memberMail;
    }

    @Override
    public int updateStatusWithIdAndMemberId(long id, long memeberId, int status) {
        return  memberMailMapper.setStatusWithIdAndMemberId(id,memeberId,status);
    }
    @Override
    public List<MemberMailEntity> getLastMails(Long memberId, int status, int size, Long start) {
        //限制大量查询
        if(size>50) size = 50;
        List<MemberMailEntity> memberMailEntities = memberMailMapper.getMailsByStatusAndMemberId(memberId,status,size,start);

        return memberMailEntities;
    }

    @Override
    public void setStatusWithIdAndMemberId(Long id, long memberId, int i) {
        memberMailMapper.setStatusWithIdAndMemberId(id,memberId,1);
    }


    //kafka

    @Override
    public void send(MemberMailEntity memberMailEntity) {
        kafkaTemplate.send(NoticeTag.MAIL_NOTICE, JSON.toJSONString(memberMailEntity));
    }



    @KafkaListener(topics = NoticeTag.MAIL_NOTICE,groupId = "group-handle") // , Acknowledgment ack
    @Override
    public boolean processKafkaConsumerMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            MemberMailEntity memberMailEntity = JSON.parseObject(record.value(),MemberMailEntity.class);
            // emailEnity =   JSON.parseObject(record.value(),EmailEnity.class);
            //
            log.info("----------------- record 2=" + record);
            //log.info("------------------ message 2 =" + emailEnity);
            memberMailEntity.setCreateTime(System.currentTimeMillis());
            insert(memberMailEntity);
            ack.acknowledge();
        }
        return true;
    }
}
