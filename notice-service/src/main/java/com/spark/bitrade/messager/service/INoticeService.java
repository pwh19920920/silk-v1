package com.spark.bitrade.messager.service;

import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.entity.messager.SysNoticeEntity;

import java.util.List;

/**
 * @author ww
 * @time 2019.09.19 10:56
 */
public interface INoticeService {
    //发送通知
    public void saveNotice(NoticeEntity noticeEntity);
    public int sendToKafka(NoticeEntity noticeEntity) ;

    int getUnReadNoticeNumByMemberId(long memnberId);
    public List<SysNoticeEntity> getLastUnReadSysNotice(long memberid, int size, long start) ;
    public List<SysNoticeEntity> getLastSysNotice(long memberid,int status, int size,long start);

    public void setStatusByIdWithMemberId(long id,long memberId,int status);

}
