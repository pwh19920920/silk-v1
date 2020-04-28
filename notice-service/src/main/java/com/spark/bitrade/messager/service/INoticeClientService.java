package com.spark.bitrade.messager.service;

import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.entity.messager.NoticeEntity;

import java.util.List;

/**
 * @author Administrator
 * @time 2019.09.18 17:03
 */
public interface INoticeClientService {

    public List<JPushDeviceType> sendToClient(NoticeEntity noticeEntity,long memberId);


    public void sendToAllClient(NoticeEntity noticeEntity);

}
