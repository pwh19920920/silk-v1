package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 用户订单取消记录
 * @author Zhang Yanjun
 * @time 2018.11.02 14:40
 */
@Data
@ApiModel
public class MemberOrderCancelVO {

    //订单创建时间（开始时间）
    @ApiModelProperty(name = "createTimeStart",value = "订单创建时间（开始时间）",example = "2018-11-10 16:27:17")
    private String createTimeStart;
    //订单创建时间（结束时间）
    @ApiModelProperty(name = "createTimeEnd",value = "订单创建时间（结束时间）",example = "2018-11-10 16:27:17")
    private String createTimeEnd;
    //订单取消时间（开始时间）
    @ApiModelProperty(name = "cancelTimeStart",value = "订单取消时间（开始时间）",example = "2018-11-10 16:27:17")
    private String cancelTimeStart;
    //订单取消时间（结束时间）
    @ApiModelProperty(name = "cancelTimeEnd",value = "订单取消时间（结束时间）",example = "2018-11-10 16:27:17")
    private String cancelTimeEnd;
    //邮箱
    @ApiModelProperty(name = "email",value = "邮箱",example = "2654424556@qq.com")
    private String email;
    //手机号
    @ApiModelProperty(name = "phone",value = "手机号",example = "15923105825")
    private String phone;

    //排序字段 cancelAuto、cancelHandle、sumCancel
    @ApiModelProperty(name = "sortName",value = "排序字段 cancelAuto、cancelHandle、sumCancel",example = "sumCancel")
    private String sortName;
    //升序/降序
    @ApiModelProperty(name = "sort",value = "升序/降序 DESC/ASC",example = "DESC")
    private String sort;

}
