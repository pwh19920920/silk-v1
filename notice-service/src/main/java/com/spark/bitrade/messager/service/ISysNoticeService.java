package com.spark.bitrade.messager.service;

import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.entity.messager.SysNoticeEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ww
 * @time 2019.09.16 16:10
 */

@Service
public interface ISysNoticeService extends IKafkaConsumerService  {

    public void setStatusByIdWithMemberId(long id,long memberId, int status);
    //  获取 系统通知 按用户获取  按ID 从小到大获取指定 条数的通知

    //getNoticeCountByMemberId(long memberid);
    //获取用户通总数
    int getNoticeCountByMemberId(long memberid);
    List<NoticeEntity>  getLastSysNotice(long memberid , int status , int size, long start);
    //
    List<NoticeEntity>  getLastUnReadSysNotice(long memberid, int size, long start);

    //  删除过期 系统通知 或是超过用户最大通知容量的通知
    void deleteExpressNotice();

    // 插入 一个新系统通知


    SysNoticeEntity saveSysNotice(SysNoticeEntity sysNoticeEntity);

    //根据用户ID获取指定ID的通知

    SysNoticeEntity getSysNoticeByIdWithMemberId(long id,long memberId);


    public int getLastUnReadNoticeCountByMemberId(long memberid);

    public void send(SysNoticeEntity sysNoticeEntity);

    //按分页来查找
    List<SysNoticeEntity> getLastSysNoticePage(long id, int status, int size, int page);
}
