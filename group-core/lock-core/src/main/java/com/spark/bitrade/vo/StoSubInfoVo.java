package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 直接部门信息
 * @author Zhang Yanjun
 * @time 2018.12.25 15:15
 */
@ApiModel
@Data
public class StoSubInfoVo {

    @ApiModelProperty(name = "memberId",value = "会员id")
    private Long memberId;

    @ApiModelProperty(name = "level",value = "职务")
    private String level;

    @ApiModelProperty(name = "email",value = "邮箱")
    private String email;

    @ApiModelProperty(name = "username",value = "用户名")
    private String username;

    @ApiModelProperty(name = "realName",value = "真实姓名")
    private String realName;

    @ApiModelProperty(name = "mobilePhone",value = "电话")
    private String mobilePhone;

    @ApiModelProperty(name = "registrationTime",value = "注册时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registrationTime;

}
