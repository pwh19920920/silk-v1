package com.spark.bitrade.messager.service.impl;

import com.spark.bitrade.messager.entity.SysNoticeCountEntity;
import com.spark.bitrade.messager.service.ISysNoticeCountService;
import com.spark.bitrade.messager.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @time 2019.10.05 23:25
 */

@Component
public class SysNoticeCountServiceImpl implements ISysNoticeCountService {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    ISysNoticeService sysNoticeService;

    @Override
    public SysNoticeCountEntity getSysNoticeCountEntityByMemberId(long memberId) {

        // memberId 0 为所有总数

        Query query  = new Query();
        query.addCriteria(Criteria.where("memberId").is(memberId));
        SysNoticeCountEntity sysNoticeCountEntity  = mongoTemplate.findOne(query,SysNoticeCountEntity.class);
        if (sysNoticeCountEntity == null ){
            //Object o = new Object();
            //synchronized (o) {
                sysNoticeCountEntity = new SysNoticeCountEntity();
                sysNoticeCountEntity.setMemberId(memberId);
                sysNoticeCountEntity.setTotalCount(sysNoticeService.getNoticeCountByMemberId(memberId));
                sysNoticeCountEntity.setUnreadCount(sysNoticeService.getLastUnReadNoticeCountByMemberId(memberId));
                sysNoticeCountEntity = saveSysNoticeCountEntity(sysNoticeCountEntity);
            //}
        }
        return sysNoticeCountEntity;
    }

    @Override
    public SysNoticeCountEntity saveSysNoticeCountEntity(SysNoticeCountEntity sysNoticeCountEntity) {
        Query query  = new Query();
        query.addCriteria(Criteria.where("memberId").is(sysNoticeCountEntity.getMemberId()));

        Update update = new Update();
        update.set("unreadCount",sysNoticeCountEntity.getUnreadCount());
        update.set("totalCount",sysNoticeCountEntity.getTotalCount());

        mongoTemplate.upsert(query,update,SysNoticeCountEntity.class);
        //sysNoticeCountEntity = mongoTemplate.save(sysNoticeCountEntity);
        return sysNoticeCountEntity;
    }
}
