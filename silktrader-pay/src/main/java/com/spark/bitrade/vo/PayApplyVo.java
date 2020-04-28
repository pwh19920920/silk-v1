package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author fumy
 * @time 2018.10.23 10:24
 */
@ApiModel
@Data
public class PayApplyVo {

    @ApiModelProperty(value = "商户平台账号",name = "busiAccount")
    private String busiAccount;

    @ApiModelProperty(value = "审核状态，0:待审核，1：审核未通过，2：审核通过",name = "status")
    private int status;

    @ApiModelProperty(value = "审核不通过原因",name = "comment")
    private String comment;

    @ApiModelProperty(value = "申请时间",name = "applyTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applyTime;

}
