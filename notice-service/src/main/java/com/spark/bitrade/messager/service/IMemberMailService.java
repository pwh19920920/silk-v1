package com.spark.bitrade.messager.service;

import com.spark.bitrade.messager.model.MemberMailEntity;

import java.util.List;

/**
 *
 * 站内信 写服务
 *
 * @author Administrator
 * @time 2019.09.11 14:48
 */

public interface IMemberMailService extends IKafkaConsumerService {

    //按ID 查询
    public MemberMailEntity getMailById(long id);

    //按用户查询  分页要做
    public List<MemberMailEntity> getMailsByMemberId(long memberId);

    public List<MemberMailEntity> getMailsByMemberId(long memberId, int size,int status, long start);

    //保存
    public int insert(MemberMailEntity memberMail);

    MemberMailEntity getMailByIdWithUserId(Long id, long memberId);

    MemberMailEntity getMailWithContentByIdWithUserId(Long id, long memberId);

    MemberMailEntity getMailWithContentById(Long id);

    int updateStatusWithIdAndMemberId(long id,long memeberId,int status);

    void send(MemberMailEntity memberMailEntity);

    List<MemberMailEntity> getLastMails(Long memberId, int status, int size, Long start);

    void setStatusWithIdAndMemberId(Long id, long memberId, int i);
}
