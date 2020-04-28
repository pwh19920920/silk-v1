package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 *  
 *  红包vo  
 *  @author liaoqinghui  
 *  @time 2019.11.26 10:08  
 */
@Data
public class RedPackVo {

    @ApiModelProperty("红包活动id")
    private Long redPackActId;

    @ApiModelProperty(value = "红包id")
    private Long redPackRecordId;

    @ApiModelProperty(value = "红包币种")
    private String coinUnit;

    @ApiModelProperty(value = "红包数量")
    private BigDecimal amount;

    @ApiModelProperty(value = "手机号")
    private String mobilePhone;

    @ApiModelProperty(value = "领取时间")
    private Date receiveTime;

    @ApiModelProperty(value = "有效时间")
    private Integer validHours;
}
