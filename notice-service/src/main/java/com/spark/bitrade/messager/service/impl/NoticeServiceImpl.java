package com.spark.bitrade.messager.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.result.UpdateResult;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.constant.messager.NoticeTag;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.entity.messager.SysNoticeEntity;
import com.spark.bitrade.messager.service.INoticeClientService;
import com.spark.bitrade.messager.service.INoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ww
 * @time 2019.09.19 10:59
 */

@Slf4j
@Service

public class NoticeServiceImpl implements INoticeService {


    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    INoticeClientService noticeClientService;


    @Override
    public List<SysNoticeEntity> getLastUnReadSysNotice(long memberid, int size, long start) {

        return getLastSysNotice(memberid,0,size,start);

    }
    @Override
    public List<SysNoticeEntity> getLastSysNotice(long memberid,int status, int size,long start) {

        Query query  = new Query();
        if (start>0)query.addCriteria(Criteria.where("_id").gte(start));
        query.addCriteria(
                new Criteria()
                        .orOperator(
                                Criteria.where("memberId").is(memberid)
                                ,Criteria.where("memberId").is(0)
                        )
        );
        // 0 是发给所有人的通知 所有人
        query.limit(size);
        query.with(Sort.by(
                Sort.Order.desc("_id")
        ));

        List<SysNoticeEntity> sysNoticeEntityList =  mongoTemplate.find(query,SysNoticeEntity.class, NoticeTag.SYS_NOTICE);

        return sysNoticeEntityList;
    }

    @Override
    public void setStatusByIdWithMemberId(long id,long memberId, int status) {
        Query query  = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("memberId").is(memberId));
        Update update = new Update();
        update.set("status",status);
        UpdateResult ur = mongoTemplate.updateFirst(query,update, NoticeEntity.class);
        log.info("{}",ur);
    }

    @Override
    public int getUnReadNoticeNumByMemberId(long memnberId) {

        Query query = new Query();
        query.addCriteria(
                new Criteria()
                        .andOperator(Criteria.where("status").is(0))
                        .orOperator(
                                Criteria.where("memberId").is(memnberId)
                                , Criteria.where("memberId").is(0)
                        )
        );

        int size = mongoTemplate.find(query,NoticeEntity.class).size();
        return size;
    }
    @Override
    public int sendToKafka(NoticeEntity noticeEntity) {

        log.info("发送消息到Kafka: {} {}",noticeEntity.getNoticeType().getLable(),noticeEntity);


        kafkaTemplate.send(noticeEntity.getNoticeType().getLable(), JSON.toJSONString(noticeEntity));
        //发给 websocket 的消息体
        //NoticeEntity ne = new NoticeEntity();
        //发送到客户端口
//        int succNum   = noticeClientService.sendToClient(noticeEntity.getUserId(), JPushDeviceType.ALL,noticeEntity);
        return 0;

    }


    Long lastSysNoticeId = -1L;

    @Override
    public void saveNotice(NoticeEntity noticeEntity) {

        //如果 id < 0 查找出当前 mongodb通知里的最大ID
        synchronized (lastSysNoticeId) {
            if (lastSysNoticeId < 0) {

                Query query = new Query();
                query.limit(1);
                query.with(Sort.by(
                        Sort.Order.desc("_id")
                ));

                List<NoticeEntity> noticeEntities = mongoTemplate.find(query, NoticeEntity.class);
                if (noticeEntities.size() == 0) lastSysNoticeId = 0L; //没有记录直接设置0
                else if (noticeEntities.size() == 1) { //有记录选最大ID
                    lastSysNoticeId = noticeEntities.get(0).getId();
                }
            }
            lastSysNoticeId++;
            noticeEntity.setId(lastSysNoticeId);
        }


        //noticeEntity.setData(null);
        mongoTemplate.insert(noticeEntity);




    }

}
