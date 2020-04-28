package com.spark.bitrade.notice;

import com.spark.bitrade.entity.chat.RealTimeChatMessage;

import java.util.List;

/***
 * 消息通知服务接口
 *
 * @author yangch
 * @time 2018.12.24 17:44
 */
public interface INoticeService {
    /**
     * 保存消息
     * @param message
     */
    void save(RealTimeChatMessage message);

    /**
     * 未读消息的数量
     * @param uid 用户ID
     */
    long count(String uid);

    /**
     * 读取消息
     * @param uid 用户ID
     * @param nid 消息ID
     */
    void ack(String uid, String nid);

    /**
     * 未读消息的列表
     * @param uid 用户ID
     * @return 消息ID，按消息通知时间顺序排序
     */
    List<String> list(String uid);
}
