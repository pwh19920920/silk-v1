package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 用户取消记录
 * @author Zhang Yanjun
 * @time 2018.11.02 16:22
 */
@Data
@ApiModel
public class MemberCancelDTO {
    //用户id
    @ApiModelProperty(name = "memberId",value = "用户id")
    private Long memberId;
    //邮箱
    @ApiModelProperty(name = "email",value = "邮箱")
    private String email;
    //手机号
    @ApiModelProperty(name = "phone",value = "手机号")
    private String phone;
    //真实姓名
    @ApiModelProperty(name = "realName",value = "真实姓名")
    private String realName;
    //取消类型 1手动取消 0自动取消
    @ApiModelProperty(name = "cancelType",value = "取消类型 1手动取消 0自动取消")
    private int cancelType;
    //对应订单号
    @ApiModelProperty(name = "orderSn",value = "对应订单号")
    private String orderSn;
    //订单创建时间
    @ApiModelProperty(name = "createTime",value = "订单创建时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    //取消时间
    @ApiModelProperty(name = "cancelTime",value = "取消时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;
}
