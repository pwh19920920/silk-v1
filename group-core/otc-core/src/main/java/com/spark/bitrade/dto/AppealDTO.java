package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

/**
 * 后台申诉导出dto
 * @author Zhang Yanjun
 * @time 2018.08.30 16:44
 */
@Data
@ExcelSheet
public class AppealDTO {
    @Excel(name = "订单编号")
    private BigDecimal orderSn;

    @Excel(name = "广告类型")
    private String at;
    @Enumerated(EnumType.ORDINAL)
    private AdvertiseType advertiseType;//广告类型


//    private String advertiseCreaterUserName;  //发布广告会员昵称
//    private String advertiseCreaterName;//发布广告会员真实姓名
    @Excel(name = "商家用户")
    private String advertiseName;


//    private String customerUserName;//顾客昵称
//    private String customerName;//顾客真实姓名
    @Excel(name = "普通用户")
    private String customerName;

    @Excel(name = "申诉方")
    private String initiatorName;//申诉发起者真实姓名
    private String initiatorUsername;//申诉发起者昵称

    private String associateUsername;//申诉关联者昵称
    private String associateName;//申诉关联者真实姓名

    @Excel(name = "币种")
    private String coinName;

    @Excel(name = "申诉时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;

    @Excel(name = "订单数")
    private String number;

    @Excel(name = "订单金额(元)")
    private String money;

    @Excel(name = "成交单价")
    private String price;

    @Excel(name = "支付方式")
    private String payMode;

    @Excel(name = "处理结果")
    private String result;
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isSuccess;//处理结果

    @Excel(name = "订单状态")
    private String os;
    @Enumerated(EnumType.ORDINAL)
    private OrderStatus orderStatus;//订单状态

}
