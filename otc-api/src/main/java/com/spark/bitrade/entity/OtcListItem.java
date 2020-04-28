package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * C2C消息组件
 *
 * @author Zhongxj
 * @date 2019.09.11
 */
@Builder
@Data
public class OtcListItem implements Comparable {
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
     * 内容
     */
    private String sendContent;
    /**
     * 推送更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    /**
     * 数据填充
     *
     * @param order
     * @return
     */
    public static OtcListItem toOtcListItem(Order order, Date date) {
        Date initDate = new Date();
        OrderStatus orderStatus = order.getStatus();
        if (OrderStatus.NONPAYMENT.equals(orderStatus)) {
            initDate = order.getCreateTime();
        }
        if (OrderStatus.PAID.equals(orderStatus)) {
            initDate = order.getPayTime();
        }
        if (OrderStatus.APPEAL.equals(orderStatus)) {
            initDate = date;
        }
        return OtcListItem.builder().orderSn(order.getOrderSn())
                .sendTime(initDate)
                .build();
    }

    /**
     * 根据更新时间排序
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        OtcListItem c = (OtcListItem) o;
        if (this.sendTime.before(c.sendTime)) {
            return 1;
        } else if (this.sendTime.after(c.sendTime)) {
            return -1;
        }
        return 0;
    }
}
