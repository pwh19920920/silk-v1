package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * silkPay 流水
 * @author Zhang Yanjun
 * @time 2019.03.05 17:25
 */
@Data
@ApiModel
public class PayRecordDto {

    @ApiModelProperty(value = "流水号",name = "id")
    private String id;
    @ApiModelProperty(value = "（交易对象）昵称",name = "tradeUsername")
    private String tradeUsername;
    @ApiModelProperty(value = "（交易对象）商家名称",name = "tradeBusinessName")
    private String tradeBusinessName;
    @ApiModelProperty(value = "（交易对象）用户id",name = "tradeMemberId")
    private Long tradeMemberId;
    @ApiModelProperty(value = "昵称",name = "username")
    private String username;
    @ApiModelProperty(value = "商家名称",name = "businessName")
    private String businessname;
    @ApiModelProperty(value = "用户id",name = "memberId")
    private Long memberId;
    @ApiModelProperty(value = "总数",name = "totalAmount")
    private BigDecimal totalAmount;
    @ApiModelProperty(value = "实到数",name = "arriveAmount")
    private BigDecimal arriveAmount;
    @ApiModelProperty(value = "币种",name = "unit")
    private String unit;
    @ApiModelProperty(value = "手续费币种",name = "feeUnit")
    private String feeUnit;
    @ApiModelProperty(value = "手续费",name = "fee")
    private BigDecimal fee;
    @ApiModelProperty(value = "创建时间",name = "createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @ApiModelProperty(value = "类型 0转入，1转出（对于交易平台/云端来说）",name = "type")
    private int type;
    @ApiModelProperty(value = "交易类型 0未知，1云端-云端，2兑换，3支付码，4法币，5资产划转，6云端-区块链",name = "tradeType")
    private int tradeType;
    @ApiModelProperty(value = "状态 0 发起转账，1 转账成功，2 转账失败",name = "status")
    private int status;
    @ApiModelProperty(value = "转入地址",name = "toAddress")
    private String toAddress;
    @ApiModelProperty(value = "转出地址",name = "fromAddress")
    private String fromAddress;

}
