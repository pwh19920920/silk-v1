package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.annotation.IgnoreExcel;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

@Data
//edit by yangch 时间： 2018.04.29 原因：合并
//@ExcelSheet(name="OTC订单")
@ExcelSheet
public class OtcOrderVO {

    private Long id ;

//    @Excel(name = "广告id")
    private Long advertiseId;

    @Excel(name="订单编号")
    private BigDecimal orderSnOut;
    private String orderSn ;//订单编号

    @Excel(name="交易时间")
    private String createTimeOut;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;//交易时间

    @Excel(name="广告发布者")
    private String memberName ;

    @Excel(name="交易者ID")
    private Long customerId;

    @Excel(name="交易者昵称")
    private String customerName ;

    @Excel(name="币种单位")
    private String unit ;

    @Excel(name="广告类型")
    private String advertiseTypeOut;
    @Enumerated(value = EnumType.ORDINAL)
    private AdvertiseType advertiseType ;//广告类型

    @Excel(name="交易金额")
    private BigDecimal money ;

    @Excel(name="交易数量")
    private BigDecimal number ;

    @Excel(name="手续费")
    private  BigDecimal fee ;

    @Excel(name="付款方式")
    private  String payMode ;

    @Excel(name="订单状态")
    private String orderStatusOut;
    @Enumerated(value = EnumType.ORDINAL)
    private OrderStatus status ;//订单状态

    @Excel(name="订单取消时间")
    private String cancelTimeOut;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime ;//订单取消时间

    @Excel(name="放行时间")
    private String releaseTimeOut;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime ;//放行时间

    @Excel(name="订单支付时间")
    private String payTimeOut;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime ;//订单支付时间

    @Excel(name="成交价格")
    private BigDecimal price;

    /**
     * 是否手动取消订单 1：是  其他：否
     */
    private BooleanEnum isManualCancel;

    /**
     * 新增实体属性
     */
    private String memberRealName;
    private Long memberId;
    private Long coinId;
}
