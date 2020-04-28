package com.spark.bitrade.model.screen;

import com.spark.bitrade.constant.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class AppealScreen {
    @ApiModelProperty(value = "广告类型",name = "advertiseType")
    private AdvertiseType advertiseType ;
    @ApiModelProperty(value = "申诉者",name = "complainant")
    private String complainant ;//申诉者
    @ApiModelProperty(value = "交易者",name = "negotiant")
    private String negotiant;//交易者
    @ApiModelProperty(value = "是否胜诉，0:否，1：是",name = "success")
    private BooleanEnum success;
    @ApiModelProperty(value = "币种",name = "unit")
    private String unit ;
    //add|edit|del by tansitao 时间： 2018/6/5 原因：添加按照状态查询
    /**
     * 状态
     */
    @ApiModelProperty(value = "订单状态，0:已取消 1:未付款 2:已付款 3:已完成 4:申诉中",name = "status")
    private OrderStatus status;

    @ApiModelProperty(value = "订单创建时间（开始时间）",name = "orderCreateStartTime")
    private String orderCreateStartTime;

    @ApiModelProperty(value = "订单创建时间（结束时间）",name = "orderCreateEndTime")
    private String orderCreateEndTime;

    @ApiModelProperty(value = "订单申诉时间（开始时间）",name = "appealCreateStartTime")
    private String appealCreateStartTime;

    @ApiModelProperty(value = "订单申诉时间（结束时间）",name = "appealCreateEndTime")
    private String appealCreateEndTime;

}
