package com.spark.bitrade.messager.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.result.UpdateResult;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.constant.Language;
import com.spark.bitrade.constant.messager.NoticeTag;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.entity.messager.SysNoticeEntity;
import com.spark.bitrade.messager.dto.JPushEntity;
import com.spark.bitrade.messager.entity.BaseNoticeEntity;
import com.spark.bitrade.messager.entity.SysNoticeCountEntity;
import com.spark.bitrade.messager.service.*;
import com.spark.bitrade.messager.util.Util;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * @author ww
 * @time 2019.09.16 16:18
 */

@Slf4j
@Component
public class SysNoticeServiceImpl implements ISysNoticeService {

    public static Long lastSysNoticeId = -1L;

    @Autowired
    IJPushService jPushService;

    @Autowired
    INoticeClientService noticeClientService;

    @Autowired
    INoticeService noticeService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    KafkaTemplate kafkaTemplate;


    @Autowired
    ISysNoticeCountService sysNoticeCountService;


    @Override
    public List<NoticeEntity> getLastUnReadSysNotice(long memberid, int size, long start) {

        //限制大量查询
        if (size > 50) size = 50;
        return getLastSysNotice(memberid, 0, size, start);

    }

    @Override
    public void setStatusByIdWithMemberId(long id, long memberId, int status) {
        Query query = new Query();
        //ID 读取反馈Id 设置为0的时候就把 当前用户所有通知设置为已读
        if (id > 0) query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("memberId").is(memberId));
        query.addCriteria(Criteria.where("status").is(0));
        Update update = new Update();
        update.set("status", status);
        UpdateResult ur = mongoTemplate.updateMulti(query, update, SysNoticeEntity.class);
        log.info("{}", ur);

        if (ur.getModifiedCount() > 0) {
            SysNoticeCountEntity sysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(memberId);

            sysNoticeCountEntity.setUnreadCount(sysNoticeCountEntity.getUnreadCount() - Long.valueOf(ur.getModifiedCount()).intValue());
            sysNoticeCountService.saveSysNoticeCountEntity(sysNoticeCountEntity);

            //向客户发送消息
            //取出有多少未读实时发给用户
            NoticeEntity noticeEntity = new NoticeEntity();
            noticeEntity.setNoticeType(NoticeType.SYS_NOTICE);
            //noticeEntity.setMemberId(memberId);
            //int unreadCount = getLastUnReadNoticeCountByMemberId(noticeEntity.getMemberId());
            noticeEntity.getExtras().put("unreadCount", sysNoticeCountEntity.getUnreadCount());
            noticeEntity.getExtras().put("totalCount", sysNoticeCountEntity.getTotalCount());
            List<JPushDeviceType> succDeviceTypes = noticeClientService.sendToClient(noticeEntity,memberId);
            //
        }
    }

    @Override
    public int getNoticeCountByMemberId(long memberid) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("memberId").is(memberid)
        );
        // add unreadContent
        int totalCount = mongoTemplate.find(query, SysNoticeEntity.class).size();

        return totalCount;

    }

    @Override
    public int getLastUnReadNoticeCountByMemberId(long memberid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(0));
        query.addCriteria(
                Criteria.where("memberId").is(memberid)
        );
        // add unreadContent
        int unreadCount = mongoTemplate.find(query, SysNoticeEntity.class).size();

        return unreadCount;

    }

    public List<NoticeEntity> getLastSysNotice(long memberId, int status, int size, long start) {

        //限制大量查询
        if (size > 50) size = 50;

        List<NoticeEntity> noticeEntities = new ArrayList<>();

        Query query = new Query();
        if (status >= 0) query.addCriteria(Criteria.where("status").is(status));
        if (memberId >= 0) {
            query.addCriteria(
                    new Criteria()
                            .orOperator(
                                    Criteria.where("memberId").is(memberId)
                                    , Criteria.where("memberId").is(0)
                            )
            );
        }
        // add unreadContent

        if (start >= 0) query.addCriteria(Criteria.where("_id").lt(start));
        // 0 是发给所有人的通知 所有人
        query.limit(size);
        query.with(Sort.by(
                Sort.Order.desc("_id")
        ));


        //内容分表以后需要从 Sys_NOTICE 表查询通知内容

        List<SysNoticeEntity> sysNoticeEntities = mongoTemplate.find(query, SysNoticeEntity.class);

        for (SysNoticeEntity sysNoticeEntity : sysNoticeEntities) {
            NoticeEntity noticeEntity = new NoticeEntity();
            noticeEntity.setNoticeType(NoticeType.SYS_NOTICE);
            /*if (StringUtil.isNullOrEmpty(sysNoticeEntity.getUrl())) {
                sysNoticeEntity.setSubNoticeType(NoticeType.SYS_NOTICE_BASE);
            } else {
                sysNoticeEntity.setSubNoticeType(NoticeType.SYS_NOTICE_FORWARD);
            }*/
            noticeEntity.setData(sysNoticeEntity);

            //SysNoticeCountEntity sysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(memberId);

            SysNoticeCountEntity userSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(0);
            SysNoticeCountEntity allSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(memberId);
            noticeEntity.getExtras().put("totalCount", userSysNoticeCountEntity.getTotalCount() + allSysNoticeCountEntity.getTotalCount());
            noticeEntity.getExtras().put("unreadCount", userSysNoticeCountEntity.getUnreadCount() + allSysNoticeCountEntity.getUnreadCount());


            //int unreadCount = getLastUnReadNoticeCountByMemberId(noticeEntity.getMemberId());
            //noticeEntity.getExtras().put("unreadCount", sysNoticeCountEntity.getUnreadCount());
            //noticeEntity.getExtras().put("totalCount", sysNoticeCountEntity.getTotalCount());
            noticeEntities.add(noticeEntity);
        }


        //查询的时候找到相应的通知内容 返回
//
//        for(NoticeEntity ne :noticeEntities){
//            Query q = new Query();
//            q.addCriteria(Criteria.where("_id").is(ne.getNoticeId()));
//            SysNoticeEntity  sysNoticeEntity = mongoTemplate.findOne(q,SysNoticeEntity.class);
//            ne.setData(sysNoticeEntity);
//            ne.getExtras().put("unreadCount",unreadCount);
//        }

        return noticeEntities;
    }

    @Override
    public void deleteExpressNotice() {

    }

    @Override
    public SysNoticeEntity saveSysNotice(SysNoticeEntity sysNoticeEntity) {

        //如果 id < 0 查找出当前 mongodb通知里的最大ID
        synchronized (lastSysNoticeId) {
            if (lastSysNoticeId < 0) {

                Query query = new Query();
                query.limit(1);
                query.with(Sort.by(
                        Sort.Order.desc("_id")
                ));

                List<SysNoticeEntity> sysNoticeEntityList = mongoTemplate.find(query, SysNoticeEntity.class);
                if (sysNoticeEntityList.size() == 0) lastSysNoticeId = 0L; //没有记录直接设置0
                else if (sysNoticeEntityList.size() == 1) { //有记录选最大ID
                    lastSysNoticeId = sysNoticeEntityList.get(0).getId();
                }
            }
            //从1开始
            lastSysNoticeId++;
            sysNoticeEntity.setId(lastSysNoticeId);
        }

        mongoTemplate.insert(sysNoticeEntity);
        //加未读数量
        SysNoticeCountEntity sysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(sysNoticeEntity.getMemberId());
        sysNoticeCountEntity.setTotalCount(sysNoticeCountEntity.getTotalCount() + 1);
        if(sysNoticeEntity.getStatus()==0)  sysNoticeCountEntity.setUnreadCount(sysNoticeCountEntity.getUnreadCount()+1);
        sysNoticeCountService.saveSysNoticeCountEntity(sysNoticeCountEntity);

        return sysNoticeEntity;

    }

    @Override
    public SysNoticeEntity getSysNoticeByIdWithMemberId(long id, long memberId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        if (memberId >= 0) {
            query.addCriteria(
                    new Criteria()
                            .orOperator(
                                    Criteria.where("memberId").is(memberId)
                                    , Criteria.where("memberId").is(0)
                            )
            );
        }
        // add unreadContent
        //内容分表以后需要从 Sys_NOTICE 表查询通知内容
        SysNoticeEntity sysNoticeEntities = mongoTemplate.findOne(query, SysNoticeEntity.class);
        //取了就设置为已读状态
        if (sysNoticeEntities != null) setStatusByIdWithMemberId(id, memberId, 1);
        return sysNoticeEntities;
    }

    @Override
    public void send(SysNoticeEntity sysNoticeEntity) {
        kafkaTemplate.send(NoticeTag.SYS_NOTICE, JSON.toJSONString(sysNoticeEntity));
    }

    @Override
    public List<SysNoticeEntity> getLastSysNoticePage(long memberId, int status, int size, int page) {
        //限制大量查询
        if (size > 50) size = 50;

        //从第一页开始
        if (page < 1) page = 1;
        page--;

        Query query = new Query();
        if (status >= 0) query.addCriteria(Criteria.where("status").is(status));
        if (memberId >= 0) {
            query.addCriteria(
                    new Criteria()
                            .orOperator(
                                    Criteria.where("memberId").is(memberId)
                                    , Criteria.where("memberId").is(0)
                            )
            );
        }
        // 0 是发给所有人的通知 所有人
        query.limit(size).skip(page * size);
        query.with(Sort.by(
                Sort.Order.desc("_id")
        ));


        //内容分表以后需要从 Sys_NOTICE 表查询通知内容
        List<SysNoticeEntity> sysNoticeEntities = mongoTemplate.find(query, SysNoticeEntity.class);

        return sysNoticeEntities;

    }

    @KafkaListener(topics = NoticeTag.SYS_NOTICE, groupId = "group-handle") // , Acknowledgment ack
    @Override
    public boolean processKafkaConsumerMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {

        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            //SmsEnity smsEnity = JSON.parseObject(record.value(),SmsEnity.class);
            //Object message = kafkaMessage.get();
            //
            log.info("----------------- record =" + record);
            //log.info("------------------ message 2 =" + smsEnity);
            NoticeEntity noticeEntity = JSON.parseObject(record.value(), NoticeEntity.class);

            SysNoticeEntity sysNoticeEntity = JSON.parseObject(noticeEntity.getData().toString(), SysNoticeEntity.class);

            //发送给所有人 或是不需要操作的通知
            //noticeEntity.setMemberId(sysNoticeEntity.getMemberId());
            //


            if (sysNoticeEntity.getMemberId() == 0 || sysNoticeEntity.getSubNoticeType().equals(NoticeType.SYS_NOTICE_BASE))
                sysNoticeEntity.setStatus(1);


            //保存
            if (noticeEntity.getIsOffline() == 1) {

                //发给所有人
//                if (sysNoticeEntity.getMemberId() == 0) {
//                    SysNoticeCountEntity allSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(0);
//                    allSysNoticeCountEntity.setTotalCount(allSysNoticeCountEntity.getTotalCount() + 1);
//                    sysNoticeCountService.saveSysNoticeCountEntity(allSysNoticeCountEntity);
//                } else {
//                    SysNoticeCountEntity sysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(sysNoticeEntity.getMemberId());
//                    sysNoticeCountEntity.setTotalCount(sysNoticeCountEntity.getTotalCount() + 1);
//                    sysNoticeCountService.saveSysNoticeCountEntity(sysNoticeCountEntity);
//
//
//                    //加未读数量
//                    if (sysNoticeEntity.getStatus() == 0) {
//                        sysNoticeCountEntity.setUnreadCount(sysNoticeCountEntity.getUnreadCount() + 1);
//                        sysNoticeCountService.saveSysNoticeCountEntity(sysNoticeCountEntity);
//                    }
//                }


                sysNoticeEntity = saveSysNotice(sysNoticeEntity);
                //
                noticeEntity.setNoticeId(sysNoticeEntity.getId());
                noticeService.saveNotice(noticeEntity);


            }else{
                Random random = new Random();
                sysNoticeEntity.setId(Long.valueOf(random.nextInt(Integer.MAX_VALUE-1000000)+1000000));
            }


            noticeEntity.setData(sysNoticeEntity);

            //保存离线系统通知

            //非离线通知 不显示但要保存



            //int unreadCount = getLastUnReadNoticeCountByMemberId(noticeEntity.getMemberId());
            //noticeEntity.getExtras().put("unreadCount", sysNoticeCountEntity.getUnreadCount());
            //noticeEntity.getExtras().put("totalCount", sysNoticeCountEntity.getTotalCount());



            //
            //及时消息 /离线消息， 及时的不需要入库

            //发给 websocket 的消息体

            //有URL  就去跳转没有就不用跳转

            //发送到客户端口




                BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity(noticeEntity);

                JPushEntity jPushEntity = new JPushEntity();
                if (sysNoticeEntity.getMemberId() > 0)
                    jPushEntity.getAlias().add(Util.md5(sysNoticeEntity.getMemberId().toString()));

                for (Language language : noticeEntity.getLanguage()) {
                    // zh_CN
                    jPushEntity.getTags().add(language.value());
                }
                jPushEntity.setDeviceType(noticeEntity.getDeviceType());
                // 内容
                jPushEntity.setTitle(sysNoticeEntity.getTitle());
                jPushEntity.setSubTitle(sysNoticeEntity.getContent());
                jPushEntity.setJsonData(JSON.toJSONString(baseNoticeEntity));


                if(sysNoticeEntity.getMemberId()==0) {
                    noticeClientService.sendToAllClient(noticeEntity);
                    if (noticeEntity.getIsAlert() == 1) {
                        try {
                            jPushService.send(jPushEntity);
                        } catch (Exception e) {
                            log.info("JPUSH 发送失败：{}", jPushEntity);
                        }
                    }

                }
                else{

                    List<JPushDeviceType> succDeviceTypes = noticeClientService.sendToClient(noticeEntity,sysNoticeEntity.getMemberId());

                    log.info("SocketClient 发送成功 ！：{} 个", succDeviceTypes.size());

                    if (noticeEntity.getIsAlert() == 1) {
                        try {
                            jPushService.send(jPushEntity);
                        } catch (Exception e) {
                            log.info("JPUSH 发送失败：{}", jPushEntity);
                        }
                    }
//
//                    // 先发送到连接，，没有 ios/andoid 接收  再 jpush
//                    if (!succDeviceTypes.contains(JPushDeviceType.IOS)
//                            && !succDeviceTypes.contains(JPushDeviceType.ANDROID)
//                            && !succDeviceTypes.contains(JPushDeviceType.WINPHONE)) {
//                        // 发送到 jpush
//                        if (!noticeEntity.getDeviceType().equals(JPushDeviceType.WEB)) {
//
//            /*      if (noticeEntity.getLanguage() !=  null){
//            //多种语言  使用前段模板化，，按
//                                }*/
//                            if (noticeEntity.getIsAlert() == 1) {
//                                try {
//                                    jPushService.send(jPushEntity);
//                                } catch (Exception e) {
//                                    log.info("JPUSH 发送失败：{}", jPushEntity);
//                                }
//                            }
//                        }
//                    }

                }





            //要返回成功设备 如果 包含  IOS 或 android 就已读到最高点 系统消息，

            //log.info("------------------ MessageResult =" + mr);

        }
        ack.acknowledge();
        return true;
    }
}
