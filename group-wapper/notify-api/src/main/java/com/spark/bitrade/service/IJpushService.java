package com.spark.bitrade.service;

import com.spark.bitrade.entity.messager.MemberMailEntity;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.util.MessageResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 极光推送接口
 *
 * @author zhongxj
 * @date
 */
@FeignClient("service-notice")
public interface IJpushService {
    /**
     * 极光推送，推送
     *
     * @param noticeEntity 消息体
     * @return
     */
    @RequestMapping(value = "/notice/sys/admin/send/noticeEntity", method = RequestMethod.POST)
    void sendNoticeEntity(@RequestBody NoticeEntity noticeEntity);

    /**
     * 极光推送，保存
     *
     * @param memberMailEntity 消息体
     * @return
     */
    @RequestMapping(value = "/notice/member/mail/admin/send/memberMailEntity", method = RequestMethod.POST)
    MessageResult sendMemberMailEntity(@RequestBody MemberMailEntity memberMailEntity);
}
