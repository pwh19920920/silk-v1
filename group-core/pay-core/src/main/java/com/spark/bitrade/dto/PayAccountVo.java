package com.spark.bitrade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包支付-账户列表信息
 * @author Zhang Yanjun
 * @time 2018.12.12 13:48
 */
@ApiModel
@Data
public class PayAccountVo {

    @ApiModelProperty(name = "unit", value = "币种")
    private String unit;
    @ApiModelProperty(name = "memberId", value = "会员id")
    private Long memberId;
    @ApiModelProperty(name = "balance", value = "可用余额")
    private BigDecimal balance;
    @ApiModelProperty(name = "frozenBalance", value = "冻结余额")
    private BigDecimal frozenBalance;
    @ApiModelProperty(name = "lockBalance", value = "锁仓余额")
    private BigDecimal lockBalance;
    @ApiModelProperty(name = "address", value = "钱包地址")
    private String address;
}
