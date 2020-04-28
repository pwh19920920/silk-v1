package com.spark.bitrade.dto;

import lombok.Data;

/**
 * C2C消息组件
 *
 * @author zhongxj
 * @date 2019.09.11
 */
@Data
public class OtcMsgDTO implements Comparable {
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 通知标记，true=有通知，false=没通知
     */
    private boolean noticeFlag = false;
    /**
     * 通知类型，0=未知/1=聊天/2=事件/3=事件+聊天
     */
    private int noticeType = 0;
    /**
     * 内容（中文）
     */
    private String sendContent;
    /**
     * 内容（英文）
     */
    private String sendEnContent;
    /**
     * 推送更新时间
     */
    private Long sendTime;
    /**
     * 接收消息会员ID
     */
    private Long toMemberId;

    /**
     * 根据更新时间排序
     *
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        OtcMsgDTO c = (OtcMsgDTO) o;
        if (this.sendTime < c.sendTime) {
            return 1;
        } else if (this.sendTime > c.sendTime) {
            return -1;
        }
        return 0;
    }
}
