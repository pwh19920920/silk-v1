package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.controller.OtcNoticeController;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年01月19日
 */
@Builder
@Data
public class ScanOrder implements Comparable{
    private String orderSn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String unit;
    private AdvertiseType type;
    private String name;
    private BigDecimal price;
    private BigDecimal money;
    private BigDecimal commission;
    private BigDecimal amount;
    private OrderStatus status;
    private Long memberId;
    private String avatar;
    //add by tansitao 时间： 2018/4/24 原因：添加付款码
    private String payCode;
    private Country country;//add by tansitao 时间： 2018/8/15 原因：增加国家
    /**
     * 法币ID //add by ss 时间： 2020/03/26 原因：增加法币
     */
    private Long currencyId;

    /**
     * 法币名称
     */
    private String currencyName;

    /**
     * 法币单位
     */
    private String currencyUnit;

    /**
     * 法币符号
     */
    private String currencySymbol;
    @JsonIgnore
    private String countryName;//add by tansitao 时间： 2018/8/15 原因：增加国家名字
    private BooleanEnum isManualCancel; //add by tansitao 时间： 2018/12/28 原因：增加是否为手动取消

    //add by yangch 时间： 2018.12.27 原因：新增通知的信息
    private boolean noticeFlag = false;  //通知标记，true=有通知，false=没通知
    private long noticeTime;  //更新时间
    private int noticeType=0;   //通知类型，0=未知/1=聊天/2=事件/3=事件+聊天
    private BigDecimal orderMoney; // 订单金额
    private BigDecimal serviceRate; // 服务费率
    private BigDecimal serviceMoney; // 服务费
    private AdvertiseType advertiseType; // 服务费


    public static ScanOrder toScanOrder(Order order, Long id) {
        return ScanOrder.builder().orderSn(order.getOrderSn())
                .createTime(order.getCreateTime())
                .unit(order.getCoin().getUnit())
                .price(order.getPrice())
                .currencyId(order.getCurrencyId())
                .amount(order.getNumber())
                .money(order.getMoney())
                .status(order.getStatus())
                .advertiseType(order.getAdvertiseType())
                .commission(id.equals(order.getMemberId())?order.getCommission():BigDecimal.ZERO)
                .name(order.getCustomerId().equals(id) ? order.getMemberName() : order.getCustomerName())
                .memberId(order.getCustomerId().equals(id) ? order.getMemberId():order.getCustomerId())
                .type(judgeType(order.getAdvertiseType(), order, id))
                .payCode(order.getPayCode())
                .countryName(order.getCountry())
                .noticeTime(order.getCreateTime().getTime())
                .isManualCancel(order.getIsManualCancel())
                .orderMoney(order.getOrderMoney())
                .serviceRate(order.getServiceRate())
                .serviceMoney(order.getServiceMoney())
                .build();
    }

    public static AdvertiseType judgeType(AdvertiseType type, Order order, Long id) {
        if (type.equals(AdvertiseType.BUY) && id.equals(order.getMemberId())) {
            return AdvertiseType.BUY;
        } else if (type.equals(AdvertiseType.BUY) && id.equals(order.getCustomerId())) {
            return AdvertiseType.SELL;
        } else if (type.equals(AdvertiseType.SELL) && id.equals(order.getCustomerId())) {
            return AdvertiseType.BUY;
        } else  {
            return AdvertiseType.SELL;
        }
    }

    //add by yangch 时间： 2018.12.27 原因：按通知时间倒序排序
    @Override
    public int compareTo(Object o) {
        ScanOrder c = (ScanOrder) o;
        if(this.noticeTime < c.noticeTime){
            return 1;
        } else if(this.noticeTime > c.noticeTime) {
            return -1;
        }
        return 0;
    }
}
