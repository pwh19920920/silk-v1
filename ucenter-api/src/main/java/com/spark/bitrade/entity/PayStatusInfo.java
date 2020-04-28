package com.spark.bitrade.entity;

import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 支付方式状态信息
 * @author fumy
 * @time 2018.11.01 14:30
 */
@ApiModel
@Data
public class PayStatusInfo {

    @ApiModelProperty(value = "微信支付，0：否 1：是",name = "weChat",dataType = "Enum")
    private BooleanEnum weChat=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "银行卡支付，0：否 1：是",name = "bank",dataType = "Enum")
    private BooleanEnum bank=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "支付宝支付，0：否 1：是",name = "aliPay",dataType = "Enum")
    private BooleanEnum aliPay=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "epay支付，0：否 1：是",name = "epay",dataType = "Enum")
    private BooleanEnum epay=BooleanEnum.IS_FALSE;

}
