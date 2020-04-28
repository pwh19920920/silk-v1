package com.spark.bitrade.vo;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CoinFeeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收款方信息
 * @author Zhang Yanjun
 * @time 2019.01.21 13:48
 */
@ApiModel
@Data
public class PayToMemberVo {
    @ApiModelProperty(value = "昵称",name = "userName")
    private String userName;
    @ApiModelProperty(value = "会员id",name = "memberId")
    private Long memberId;
    @ApiModelProperty(value = "是否支持快速转账0否，1是",name = "isFast")
    private BooleanEnum isFast=BooleanEnum.IS_TRUE;
    @ApiModelProperty(value = "是否是商家0否，1是",name = "isBusiness")
    private BooleanEnum isBusiness;
    @ApiModelProperty(value = "可用余额",name = "balance")
    private BigDecimal balance=BigDecimal.ZERO;
    @ApiModelProperty(value = "手续费类型 0固定 1比例",name = "feeType")
    private CoinFeeType feeType = CoinFeeType.FIXED;
    @ApiModelProperty(value = "手续费币种",name = "feeType")
    private String feeUnit;
    @ApiModelProperty(value = "快速划转手续费（率）",name = "fee")
    private BigDecimal fastFee;
    @ApiModelProperty(value = "地址是否存在 0否 1是",name = "isAddress")
    private BooleanEnum isAddress = BooleanEnum.IS_TRUE;
}
