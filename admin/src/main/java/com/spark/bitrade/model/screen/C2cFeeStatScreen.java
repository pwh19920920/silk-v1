package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author shenzucai
 * @time 2018.06.13 17:09
 */
@Data
@ApiModel(description = "C2C手续费参数对象")
public class C2cFeeStatScreen {

    /**
     * 交易时间搜索
     */
    @ApiModelProperty(value = "开始时间",name = "startTime")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;

    @ApiModelProperty(value = "结束时间",name = "endTime")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;


    /**
     * 交易类型，0：买，1：卖
     */
    @ApiModelProperty(value = "交易类型，0：买，1：卖",name = "type")
    private Integer type ;

    @ApiModelProperty(value = "交易币种",name = "unit")
    private String unit;
}
